package com.minhthong.core.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.ControllerState
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class PlayerManagerImpl(
    private val mainDispatcher: CoroutineDispatcher,
): PlayerManager, PlayerModel() {

    override val controllerInfoFlow = MutableStateFlow<ControllerState?>(null)

    override val currentProgressMlsFlow = MutableStateFlow(0L)

    override val currentBufferMlsFlow = MutableStateFlow(0L)

    override fun initialize(context: Context) {
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

    override fun play() {
        handlePlayOrPause()
    }

    override fun loop() {
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

    private fun playMediaItem(index: Int) {
        if(currentPlaylistItems.isEmpty()) return

        currentItemIndex = getSafeIndex(index)

        cancelProgressUpdater()

        playTrack()

        cancelBufferUpdater()

        updateControllerInfo(duration = 0)
    }

    private fun playTrack() {
        val playlistItem = currentPlaylistItems[currentItemIndex]

        val mediaItem = createMediaItem(entity = playlistItem)

        exoPlayer.stop()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(0, 0)
        exoPlayer.play()
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

        updatePlayingPositionJob = playerScope.launch(mainDispatcher) {
            while (isActive) {
                currentProgressMlsFlow.update { exoPlayer.currentPosition }
                delay(200)
            }
        }
    }

    private fun startBufferUpdater() {
        if (updateBufferPositionJob?.isActive == true) return

        updateBufferPositionJob = playerScope.launch(mainDispatcher) {
            while (isActive) {
                currentBufferMlsFlow.update { exoPlayer.bufferedPosition }
                delay(300)
            }
        }
    }

    private fun endProgressUpdater() {
        updatePlayingPositionJob?.cancel()
    }

    private fun cancelBufferUpdater() {
        currentBufferMlsFlow.update { 0 }
        updateBufferPositionJob?.cancel()
    }

    private fun getSafeIndex(index: Int): Int {
        val trackSize = currentPlaylistItems.size

        return if (index >= trackSize) {
            0
        } else if (index < 0) {
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
            currentItemIndex = 0
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

        exoPlayer = ExoPlayer.Builder(context)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        exoPlayer.addListener(playerEventListener)
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
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                exoPlayer.prepare()
            }
            exoPlayer.play()
        }
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
        cancelProgressUpdater()
        cancelBufferUpdater()
        playerScope.cancel()

        controllerInfoFlow.update { null }
        cancelProgressUpdater()

        exoPlayer.removeListener(playerEventListener)
        exoPlayer.clearMediaItems()
        exoPlayer.release()
    }

    private fun cancelProgressUpdater() {
        updatePlayingPositionJob?.cancel()
        currentProgressMlsFlow.update { 0 }
    }

    private val playerEventListener = object : Player.Listener {

        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_ENDED -> {
                    moveToNext()
                }
                Player.STATE_READY -> {
                    val duration = exoPlayer.duration
                    if (duration != C.TIME_UNSET) {
                        updateControllerInfo(duration = duration)
                        startProgressUpdater()
                        startBufferUpdater()
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startProgressUpdater()
            } else {
                endProgressUpdater()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            cancelProgressUpdater()
            startProgressUpdater()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            controllerInfoFlow.update { current ->
                current?.copy(isPlaying = exoPlayer.playWhenReady)
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

