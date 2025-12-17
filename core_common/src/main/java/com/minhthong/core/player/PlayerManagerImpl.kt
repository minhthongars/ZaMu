package com.minhthong.core.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.ControllerEntity
import com.minhthong.core.model.PlaylistItemEntity
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

internal class PlayerManagerImpl(
    private val mainDispatcher: CoroutineDispatcher,
): PlayerManager {

    private lateinit var exoPlayer: ExoPlayer

    private lateinit var playerScope: CoroutineScope

    private var updatePlayingPositionJob: Job? = null

    private var currentPlaylistItems = emptyList<PlaylistItemEntity>()
    private var currentTrackIndex = -1
    private var isLooping = false

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
            endProgressUpdater()
            currentProgressMlsFlow.update { 0 }
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

    /*implementation*/

    override val controllerInfoFlow = MutableStateFlow<ControllerEntity?>(null)

    override val currentProgressMlsFlow = MutableStateFlow(0L)

    override val hasSetPlaylistFlow = controllerInfoFlow.map { it != null }

    override fun initialize(context: Context) {
        val playerExceptionHandler = CoroutineExceptionHandler { _, e -> e.printStackTrace() }
        val coroutineContext = mainDispatcher + playerExceptionHandler + SupervisorJob()
        playerScope = CoroutineScope(coroutineContext)

        exoPlayer = ExoPlayer.Builder(context)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        exoPlayer.addListener(playerEventListener)
    }

    override fun setPlaylist(
        playlistItems: List<PlaylistItemEntity>
    ) {
        if (playlistItems.isEmpty()) {
            onClearAllItems()
            return
        }

        if (currentTrackIndex == -1) {
            currentPlaylistItems = playlistItems
            return
        }

        val playingTrack = currentPlaylistItems.getOrNull(currentTrackIndex)
        currentPlaylistItems = playlistItems

        calculateNewTrackIndex(playingTrack)
    }

    override fun seekToMediaItem(playlistItemId: Int) {
        val index = currentPlaylistItems.indexOfFirst { it.id == playlistItemId }
        playMediaItem(index = index)
    }

    override fun seekToLastMediaItem(playlistItem: PlaylistItemEntity) {
        currentPlaylistItems = currentPlaylistItems + playlistItem
        playMediaItem(index = currentPlaylistItems.size - 1)
    }

    override fun release() {
        updatePlayingPositionJob?.cancel()
        playerScope.cancel()

        controllerInfoFlow.update { null }
        currentProgressMlsFlow.update { 0 }

        exoPlayer.removeListener(playerEventListener)
        exoPlayer.clearMediaItems()
        exoPlayer.release()
    }

    override fun play() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                exoPlayer.prepare()
            }
            exoPlayer.play()
        }
    }

    override fun seek(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun loop() {
        isLooping = isLooping.not()

        controllerInfoFlow.update { current ->
            current?.copy(isLooping = isLooping)
        }
    }

    override fun moveToNext() {
        val newIndex = if (isLooping) {
            currentTrackIndex
        } else {
            currentTrackIndex + 1
        }
        playMediaItem(index = newIndex)
    }

    override fun moveToPrevious() {
        val newIndex = if (isLooping) {
            currentTrackIndex
        } else {
            currentTrackIndex - 1
        }
        playMediaItem(index = newIndex)
    }

    override fun getPlayer(): ExoPlayer {
        return exoPlayer
    }

    private fun playMediaItem(index: Int) {
        if(currentPlaylistItems.isEmpty()) return

        val safeIndex = getSafeIndex(index)

        currentTrackIndex = safeIndex

        val playlistItem = currentPlaylistItems[safeIndex]

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

    private fun endProgressUpdater() {
        updatePlayingPositionJob?.cancel()
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
                playMediaItem(index = currentTrackIndex)
            } else {
                currentTrackIndex = newPlayingIndex
            }
        } else {
            currentTrackIndex = 0
        }
    }

    private fun updateControllerInfo(duration: Long) {
        controllerInfoFlow.update {
            ControllerEntity(
                playingItem = currentPlaylistItems[currentTrackIndex],
                isLooping = isLooping,
                isPlaying = true,
                duration = duration
            )
        }
    }
}

