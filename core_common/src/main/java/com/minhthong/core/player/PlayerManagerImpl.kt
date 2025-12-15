package com.minhthong.core.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.PlayerEntity
import com.minhthong.core.model.PlaylistItemEntity
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

internal class PlayerManagerImpl(
    private val mainDispatcher: CoroutineDispatcher,
): PlayerManager {

    private lateinit var exoPlayer: ExoPlayer

    private lateinit var currentPlaylistItems: List<PlaylistItemEntity>

    private lateinit var playerExceptionHandler: CoroutineExceptionHandler

    private lateinit var playerScope: CoroutineScope

    private var updatePlayingPositionJob: Job? = null

    private var currentTrackIndex = -1
    private var isLooping = false

    private fun playMediaItem(index: Int) {
        if(currentPlaylistItems.isEmpty()) return

        val safeIndex = getSafeIndex(index)

        currentTrackIndex = safeIndex

        val track = currentPlaylistItems[safeIndex]

        playerInfoFlow.update { current ->
            current?.copy(trackInfo = track) ?: getDefaultData()
        }

        val mediaItem = createMediaItem(track = track.entity)

        exoPlayer.stop()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(0, 0)
        exoPlayer.play()
    }

    /*implementation*/

    override val playerInfoFlow = MutableStateFlow<PlayerEntity?>(null)

    override val currentProgressMlsFlow = MutableStateFlow(0L)

    override val hasSetPlaylistFlow = playerInfoFlow.map { it != null }

    override fun initialize(context: Context) {
        playerExceptionHandler = CoroutineExceptionHandler { _, e ->
            e.printStackTrace()
        }

        currentPlaylistItems = emptyList()

        val coroutineContext = mainDispatcher + playerExceptionHandler + SupervisorJob()
        playerScope = CoroutineScope(coroutineContext)

        exoPlayer = ExoPlayer.Builder(context)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        exoPlayer.addListener(playerEventListener)
    }

    override fun setPlaylist(
        playlistItemEntities: List<PlaylistItemEntity>
    ) {
        if (playlistItemEntities.isEmpty()) {
            onClearAllItems()
            return
        }

        val playingTrack = currentPlaylistItems.getOrNull(currentTrackIndex)
        currentPlaylistItems = playlistItemEntities

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
        exoPlayer.removeListener(playerEventListener)
        exoPlayer.clearMediaItems()
        exoPlayer.release()

        updatePlayingPositionJob?.cancel()
        playerScope.cancel()

        playerInfoFlow.update { null }
        currentProgressMlsFlow.update { 0 }
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

        playerInfoFlow.update { current ->
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

    private fun createMediaItem(track: TrackEntity): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setDisplayTitle(track.title)
            .setDurationMs(track.durationMs)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build()

        return MediaItem.Builder()
            .setUri(track.uri)
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
        playerInfoFlow.update { null }
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

    private fun getDefaultData(): PlayerEntity {
        return PlayerEntity(
            trackInfo = currentPlaylistItems[currentTrackIndex],
            isLooping = isLooping,
            isShuffling = false,
            isPlaying = true,
        )
    }

    private val playerEventListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                moveToNext()
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
            playerInfoFlow.update { current ->
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

