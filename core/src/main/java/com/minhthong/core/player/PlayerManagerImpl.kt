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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerManagerImpl(
    dispatcher: CoroutineDispatcher,
): PlayerManager {

    private lateinit var exoPlayer: ExoPlayer

    private var currentPlaylist: List<TrackEntity> = emptyList()

    private val playerExceptionHandler = CoroutineExceptionHandler { _, e ->
        e.printStackTrace()
    }
    private val playerScope = CoroutineScope(
        context = dispatcher + SupervisorJob() + playerExceptionHandler
    )

    private val playerInfoFlow = MutableStateFlow<PlayerEntity?>(null)

    private var progressJob: Job? = null

    private val _progressFlow = MutableStateFlow(0L)

    private val playerEventListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {}
                Player.STATE_READY -> {}
                Player.STATE_BUFFERING -> {}

                Player.STATE_ENDED -> {
                    moveToNext()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startProgressUpdater()
            } else {
                endProgressUpdater()
            }

            playerInfoFlow.update { current ->
                current?.copy(isPlaying = exoPlayer.isPlaying)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val currentIndex = exoPlayer.currentMediaItemIndex
            playerInfoFlow.update { current ->
                current?.copy(
                    trackInfo = currentPlaylist[currentIndex]
                )
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        )  = Unit
    }

    private fun getDefaultData(currentIndex: Int): PlayerEntity {
        return PlayerEntity(
            trackInfo = currentPlaylist[currentIndex],
            isLooping = exoPlayer.repeatMode == Player.REPEAT_MODE_ONE,
            isShuffling = exoPlayer.shuffleModeEnabled,
            isPlaying = false,
        )
    }

    private fun startProgressUpdater() {
        if (progressJob?.isActive == true) return

        progressJob = playerScope.launch(Dispatchers.Main) {
            while (isActive) {
                _progressFlow.value = exoPlayer.currentPosition
                delay(200L)
            }
        }
    }

    private fun endProgressUpdater() {
        progressJob?.cancel()
    }

    override fun initialize(context: Context) {
        if (::exoPlayer.isInitialized.not()) {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer.addListener(playerEventListener)
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

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.addMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.seekTo(safeStartIndex, 0L)
        exoPlayer.play()
    }

    override fun hasSetPlaylist(): Flow<Boolean> = playerInfoFlow.map { it != null }

    override fun release() {
        exoPlayer.clearMediaItems()
        exoPlayer.release()
        currentPlaylist = emptyList()
        playerScope.cancel()
        playerInfoFlow.update { null }
        progressJob?.cancel()
    }

    override fun playerInfo(): StateFlow<PlayerEntity?> {
        return playerInfoFlow.asStateFlow()
    }

    override fun currentProgressMls() = _progressFlow.asStateFlow()

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
}

