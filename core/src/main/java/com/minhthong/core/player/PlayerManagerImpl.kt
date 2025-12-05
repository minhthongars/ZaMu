package com.minhthong.core.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.PlayerEntity
import com.minhthong.core.model.TrackEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerManagerImpl(
    defaultDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
): PlayerManager {

    private lateinit var exoPlayer: ExoPlayer

    private var currentPlaylist: List<TrackEntity> = emptyList()

    private var playbackState: Int = Player.STATE_IDLE

    private var playing: Boolean = false

    private var progressJob: Job? = null

    private val playerExceptionHandler = CoroutineExceptionHandler { _, e -> e.printStackTrace() }

    private val playerScope = CoroutineScope(defaultDispatcher + SupervisorJob() + playerExceptionHandler)

    override val playerInfoFlow = MutableStateFlow<PlayerEntity?>(null)

    override val currentProgressMlsFlow = MutableStateFlow(0L)

    override val hasSetPlaylistFlow = playerInfoFlow.map { it != null }

    override fun initialize(context: Context) {
        if (::exoPlayer.isInitialized.not()) {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.addListener(PlayerEventListener())
        }
    }

    override fun setPlaylist(
        tracks: List<TrackEntity>,
        startIndex: Int
    ) {
        if (tracks.isEmpty()) return

        currentPlaylist = tracks

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder().build()
                )
                .build()
        }
        val safeStartIndex = startIndex.coerceIn(0, tracks.size - 1)

        playerInfoFlow.update { getDefaultData(currentIndex = safeStartIndex) }

        with(exoPlayer) {
            stop()
            clearMediaItems()
            addMediaItems(mediaItems)
            prepare()
            seekTo(safeStartIndex, 0L)
            play()
        }
    }

    override fun release() {
        exoPlayer.clearMediaItems()
        exoPlayer.release()
        currentPlaylist = emptyList()
        playerScope.cancel()
        playerInfoFlow.update { null }
        progressJob?.cancel()
    }

    override fun play() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    override fun seek(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun loop() {
        val isLooping = exoPlayer.repeatMode == Player.REPEAT_MODE_ONE
        if (isLooping) {
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        } else {
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        }

        playerInfoFlow.update { current ->
            current?.copy(
                isLooping = isLooping.not(),
            )
        }
    }

    override fun shuffle() {
        val shuffleModeEnabled = exoPlayer.shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = shuffleModeEnabled.not()

        playerInfoFlow.update { current ->
            current?.copy(
                isShuffling = shuffleModeEnabled.not(),
            )
        }
    }

    override fun moveToNext() {
        if (exoPlayer.hasNextMediaItem().not()) {
            exoPlayer.seekTo(0, 0)
            return
        }
        exoPlayer.seekToNextMediaItem()
    }

    override fun moveToPrevious() {
        if (exoPlayer.hasPreviousMediaItem().not()) {
            val lastPosition = exoPlayer.mediaItemCount - 1
            exoPlayer.seekTo(lastPosition, 0)
            return
        }
        exoPlayer.seekToPreviousMediaItem()
    }

    private fun getDefaultData(currentIndex: Int): PlayerEntity {
        return PlayerEntity(
            trackInfo = currentPlaylist[currentIndex],
            isLooping = exoPlayer.repeatMode == Player.REPEAT_MODE_ONE,
            isShuffling = exoPlayer.shuffleModeEnabled,
            isPlaying = true,
        )
    }

    private fun startProgressUpdater() {
        if (progressJob?.isActive == true) return

        progressJob = playerScope.launch(mainDispatcher) {
            while (isActive) {
                currentProgressMlsFlow.update { exoPlayer.currentPosition }
                delay(150L)
            }
        }
    }

    private fun endProgressUpdater() {
        progressJob?.cancel()
    }

    private fun updatePlayerState() {
        val uiIsPlaying = when (playbackState) {
            Player.STATE_BUFFERING -> true
            Player.STATE_READY -> playing
            else -> false
        }

        playerInfoFlow.update { info ->
            info?.copy(isPlaying = uiIsPlaying)
        }
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                moveToNext()
            }
            playbackState = state
            updatePlayerState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startProgressUpdater()
            } else {
                endProgressUpdater()
            }

            playing = isPlaying
            updatePlayerState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            endProgressUpdater()
            currentProgressMlsFlow.update { 0 }

            val currentIndex = exoPlayer.currentMediaItemIndex
            playerInfoFlow.update { current ->
                current?.copy(trackInfo = currentPlaylist[currentIndex])
            }

            startProgressUpdater()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        )  = Unit
    }
}

