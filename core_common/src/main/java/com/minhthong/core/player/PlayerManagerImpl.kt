package com.minhthong.core.player

import android.content.Context
import android.os.Handler
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import com.minhthong.core.model.ControllerState
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
internal class PlayerManagerImpl(
    private val context: Context,
    private val mainDispatcher: CoroutineDispatcher,
    private val audioFocusManager: AudioFocusManager,
): PlayerManager, PlayerModel() {

    companion object {
        private const val DUCK_VOLUME = 0.2f
        private const val NORMAL_VOLUME = 1f
        private const val ZERO = 0
        private const val BUFFER_DELAY = 300L
        private const val POSITION_DELAY = 150L
        private const val CROSSFADE_DELAY = 20L
    }

    override val controllerInfoFlow = MutableStateFlow<ControllerState?>(null)

    override val currentProgressMlsFlow = MutableStateFlow(0L)

    override val currentBufferMlsFlow = MutableStateFlow(0L)

    override val waveformSamplesFlow = MutableStateFlow(FloatArray(0))


    override fun initialize() {
        initPlayer(context)
    }

    override fun setPlaylist(playlistItems: List<PlaylistItemEntity>) {
        handleSetPlaylistItemsAwareEdgeCase(playlistItems)
    }

    override fun seekToMediaItem(playlistItemId: Int) {
        findItemAndSeek(playlistItemId)
    }

    override fun seekToLastMediaItem(playlistItem: PlaylistItemEntity) {
        addAndSeek(playlistItem)
    }

    override fun playOrPause() {
        handlePlayOrPause()
    }

    override fun loopOrNot() {
        handleLoopOrNot()
    }

    override fun moveToNext() {
        moveToNextAndPlay()
    }

    override fun moveToPrevious() {
        moveToPreviousAndPlay()
    }

    override fun seek(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun getPlayer(): ExoPlayer {
        return exoPlayer
    }

    override fun release() {
        cancelJobAndReleasePlayer()
    }

    private val audioFocusCallback = object : AudioFocusManager.Callback {
        override fun onFocusGained() = handleFocusGain()
        override fun onFocusLost() = handleFocusLoss()
        override fun onFocusLostTransient() = handleFocusLossTransient()
        override fun onDuck() = handleFocusDuck()
    }

    private fun playMediaItem(index: Int) {
        if(currentPlaylistItems.isEmpty()) return

        cancelBufferUpdater()
        cancelProgressUpdater()

        currentItemIndex = getSafeIndex(index)

        updateControllerInfo(duration = 0)

        transitionTrack()
    }

    private fun transitionTrack() {
        val playlistItem = currentPlaylistItems[currentItemIndex]

        val mediaItem = createMediaItem(entity = playlistItem)

        if (exoPlayer.isPlaying) {
            mediaTransitionJob?.cancel()
            mediaTransitionJob = crossfadeTransition(mediaItem)
        } else {
            playTrack(mediaItem)
        }
    }

    private fun crossfadeTransition(mediaItem: MediaItem) = playerScope.launch {
        subExoPlayer.safeAddListener(subPlayerEventListener)
        exoPlayer.removeListener(playerEventListener)

        subExoPlayer.apply {
            setMediaItem(mediaItem)
            prepare()
            volume = 0f
            play()
        }

        while (true) {
            exoPlayer.volume -= 0.01f
            subExoPlayer.volume += 0.01f
            delay(CROSSFADE_DELAY)

            currentProgressMlsFlow.update {
                (subExoPlayer.volume * 100 * CROSSFADE_DELAY).toLong()
            }

            if (subExoPlayer.volume == 1F) {
                break
            }
        }

        subExoPlayer.removeListener(subPlayerEventListener)

        val temp = subExoPlayer
        subExoPlayer = exoPlayer
        exoPlayer = temp

        subExoPlayer.stop()
        exoPlayer.safeAddListener(playerEventListener)

        startProgressUpdater()
        startBufferUpdater()
    }

    private fun ExoPlayer.safeAddListener(listener: Player.Listener) {
        removeListener(listener)
        addListener(listener)
    }

    private fun playTrack(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        val granted = audioFocusManager.requestAudioFocus(audioFocusCallback)
        if (granted) {
            exoPlayer.play()
        }

        startProgressUpdater()
        startBufferUpdater()
    }

    private fun createMediaItem(entity: PlaylistItemEntity): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(entity.title)
            .setArtist(entity.artist)
            .setDisplayTitle(entity.title)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build()

        return MediaItem.Builder()
            .setUri(entity.uri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun startProgressUpdater() {
        if (updatePlayingPositionJob?.isActive == true) return

        updatePlayingPositionJob = playerScope.launch {
            while (isActive) {
                currentProgressMlsFlow.update { exoPlayer.currentPosition }
                delay(POSITION_DELAY)
            }
        }
    }

    private fun startBufferUpdater() {
        if (updateBufferPositionJob?.isActive == true) return

        updateBufferPositionJob = playerScope.launch {
            while (isActive) {
                currentBufferMlsFlow.update { exoPlayer.bufferedPosition }
                delay(BUFFER_DELAY)
            }
        }
    }

    private fun cancelBufferUpdater() {
        currentBufferMlsFlow.update { 0 }
        updateBufferPositionJob?.cancel()
    }

    private fun cancelProgressUpdater() {
        updatePlayingPositionJob?.cancel()
        currentProgressMlsFlow.update { 0 }
    }

    private fun getSafeIndex(index: Int): Int {
        val trackSize = currentPlaylistItems.size

        return if (index >= trackSize) {
            ZERO
        } else if (index < ZERO) {
            trackSize - 1
        } else {
            index
        }
    }

    private fun onClearAllItems() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        controllerInfoFlow.update { null }
        currentPlaylistItems = emptyList()
        audioFocusManager.abandonFocus()
    }

    private fun calculateNewTrackIndex(playingTrack: PlaylistItemEntity?) {
        if (playingTrack != null) {
            val newPlayingIndex = currentPlaylistItems.indexOfFirst { it.id == playingTrack.id }
            if (newPlayingIndex == -1) {
                handleUserRemovePlayingTrack()
            } else {
                currentItemIndex = newPlayingIndex
            }
        } else {
            currentItemIndex = ZERO
        }
    }

    private fun handleUserRemovePlayingTrack() {
        playMediaItem(index = currentItemIndex)
        exoPlayer.stop()
        controllerInfoFlow.update { current ->
            current?.copy(isPlaying = false)
        }
    }

    private fun updateControllerInfo(duration: Long) {
        val playingItem = currentPlaylistItems[currentItemIndex]

        controllerInfoFlow.update {
            ControllerState(
                isLooping = isLooping,
                isPlaying = exoPlayer.playWhenReady,
                duration = duration,
                playingItem = playingItem
            )
        }
    }

    private fun initPlayer(context: Context) {
        val playerExceptionHandler = CoroutineExceptionHandler { _, e -> e.printStackTrace() }
        val coroutineContext = mainDispatcher + playerExceptionHandler + SupervisorJob()
        playerScope = CoroutineScope(coroutineContext)

        waveformProcessor = WaveformAudioProcessor(
            onSamplesReady = { samples ->
                waveformSamplesFlow.update { samples }
            }
        )

        subWaveformProcessor = WaveformAudioProcessor(
            onSamplesReady = { samples ->
                waveformSamplesFlow.update { samples }
            }
        )

        exoPlayer = buildExoPlayerWithWaveform(context, waveformProcessor)

        subExoPlayer = buildExoPlayerWithWaveform(context, subWaveformProcessor)

        exoPlayer.addListener(playerEventListener)
    }

    private fun buildExoPlayerWithWaveform(
        context: Context,
        processor: WaveformAudioProcessor
    ): ExoPlayer {
        val rendererFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioRenderers(
                context: Context,
                extensionRendererMode: Int,
                mediaCodecSelector: MediaCodecSelector,
                enableDecoderFallback: Boolean,
                audioSink: AudioSink,
                eventHandler: Handler,
                eventListener: AudioRendererEventListener,
                out: ArrayList<Renderer>
            ) {
                val sink = DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf<AudioProcessor>(processor))
                    .build()

                super.buildAudioRenderers(
                    context,
                    extensionRendererMode,
                    mediaCodecSelector,
                    enableDecoderFallback,
                    sink,
                    eventHandler,
                    eventListener,
                    out
                )
            }
        }

        return ExoPlayer.Builder(context, rendererFactory)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
    }

    private fun handleSetPlaylistItemsAwareEdgeCase(
        playlistItems: List<PlaylistItemEntity>
    ) {
        if (playlistItems.isEmpty()) {
            onClearAllItems()
            return
        }

        if (currentItemIndex == -1) {
            currentPlaylistItems = playlistItems
            return
        }

        val playingTrack = currentPlaylistItems.getOrNull(currentItemIndex)
        currentPlaylistItems = playlistItems

        calculateNewTrackIndex(playingTrack)
    }

    private fun findItemAndSeek(
        playlistItemId: Int
    ) {
        val index = currentPlaylistItems.indexOfFirst { it.id == playlistItemId }
        playMediaItem(index = index)
    }

    private fun addAndSeek(playlistItem: PlaylistItemEntity) {
        currentPlaylistItems = currentPlaylistItems + playlistItem
        playMediaItem(index = currentPlaylistItems.size - 1)
    }

    private fun handlePlayOrPause() {
        if (exoPlayer.isPlaying) {
            crossfadePause()
        } else {
            val granted = audioFocusManager.requestAudioFocus(audioFocusCallback)
            if (!granted) return

            safePlay()
        }
    }

    private fun crossfadePause() = playerScope.launch {
        controllerInfoFlow.update { current ->
            current?.copy(isPlaying = false)
        }

        while (true) {
            exoPlayer.volume -= 0.06F
            delay(CROSSFADE_DELAY)

            if (exoPlayer.volume == 0F) {
                exoPlayer.pause()
                exoPlayer.volume = 1F

                audioFocusManager.abandonFocus()

                break
            }
        }
    }

    private fun safePlay() {
        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            exoPlayer.prepare()
        }
        exoPlayer.play()
    }

    private fun handleLoopOrNot() {
        isLooping = isLooping.not()

        controllerInfoFlow.update { current ->
            current?.copy(isLooping = isLooping)
        }
    }

    private fun moveToNextAndPlay() {
        val newIndex = if (isLooping) {
            currentItemIndex
        } else {
            currentItemIndex + 1
        }
        playMediaItem(index = newIndex)
    }

    private fun moveToPreviousAndPlay() {
        val newIndex = if (isLooping) {
            currentItemIndex
        } else {
            currentItemIndex - 1
        }
        playMediaItem(index = newIndex)
    }

    private fun cancelJobAndReleasePlayer() {
        playerScope.cancel()

        controllerInfoFlow.update { null }

        audioFocusManager.abandonFocus()
        exoPlayer.removeListener(playerEventListener)
        exoPlayer.clearMediaItems()
        exoPlayer.release()
        waveformSamplesFlow.update { FloatArray(0) }
        waveformProcessor.reset()
        subWaveformProcessor.reset()
    }

    private fun handleFocusLoss() {
        shouldResumeOnFocusGain = false
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
        audioFocusManager.abandonFocus()
    }

    private fun handleFocusLossTransient() {
        if (exoPlayer.isPlaying) {
            shouldResumeOnFocusGain = true
            exoPlayer.pause()
        }
    }

    private fun handleFocusGain() {
        if (isDucked) {
            exoPlayer.volume = NORMAL_VOLUME
            isDucked = false
        }

        if (shouldResumeOnFocusGain) {
            safePlay()
            shouldResumeOnFocusGain = false
        }
    }

    private fun handleFocusDuck() {
        if (exoPlayer.isPlaying) {
            exoPlayer.volume = DUCK_VOLUME
            isDucked = true
        }
    }

    private val subPlayerEventListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_READY) {
                updateControllerInfo(duration = subExoPlayer.duration)
            }
        }
    }

    private val playerEventListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_ENDED -> {
                    moveToNext()
                }
                Player.STATE_READY -> {
                    updateControllerInfo(duration = exoPlayer.duration)
                }

                else -> Unit
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            val isCanPlay = exoPlayer.playWhenReady
            controllerInfoFlow.update { current ->
                current?.copy(isPlaying = isCanPlay)
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (updatePlayingPositionJob?.isActive == false) {
                currentProgressMlsFlow.update { newPosition.positionMs }
            }
        }
    }
}


