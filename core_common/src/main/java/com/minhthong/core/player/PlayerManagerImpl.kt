package com.minhthong.core.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class PlayerManagerImpl(
    defaultDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
): PlayerManager {

    private lateinit var exoPlayer: ExoPlayer

    private var currentPlaylist: List<TrackEntity> = emptyList()

    private var progressJob: Job? = null

    private val playerExceptionHandler = CoroutineExceptionHandler { _, e -> e.printStackTrace() }

    private val playerScope = CoroutineScope(defaultDispatcher + SupervisorJob() + playerExceptionHandler)

    override val playerInfoFlow = MutableStateFlow<PlayerEntity?>(null)

    override val currentProgressMlsFlow = MutableStateFlow(0L)

    override val hasSetPlaylistFlow = playerInfoFlow.map { it != null }

    override fun initialize(context: Context) {
        exoPlayer = ExoPlayer.Builder(context)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        exoPlayer.addListener(PlayerEventListener())
    }

    override fun setPlaylist(
        tracks: List<TrackEntity>,
        startIndex: Int
    ) {
        if (tracks.isEmpty()) return

        currentPlaylist = tracks

        val mediaItems = createMediaItems()
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
        progressJob?.cancel()
        playerScope.cancel()
        playerInfoFlow.update { null }
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

    override fun bindWithPlaylist(playlistFlow: Flow<TrackEntity>) {

    }

    override fun removeItem(index: Int) {
        exoPlayer.removeMediaItem(index)

        playerInfoFlow.update { current ->
            current?.copy(
                playingTrackIndex = exoPlayer.currentMediaItemIndex,
            )
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

    override fun getPlayer(): ExoPlayer {
        return exoPlayer
    }

    private fun createMediaItems(): List<MediaItem> {
       return currentPlaylist.mapIndexed { index, track ->
           val metadata =  MediaMetadata.Builder()
               .setTitle(track.displayName)
               .setArtist(track.artist)
               .setAlbumTitle(track.album)
               .setDisplayTitle(track.title)
               .setDurationMs(track.durationMs)
               .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
               .setTrackNumber(index)
               .build()

            MediaItem.Builder()
                .setUri(track.uri)
                .setMediaMetadata(metadata)
                .build()
        }
    }

    private fun getDefaultData(currentIndex: Int): PlayerEntity {
        return PlayerEntity(
            trackInfo = currentPlaylist[currentIndex],
            isLooping = exoPlayer.repeatMode == Player.REPEAT_MODE_ONE,
            isShuffling = exoPlayer.shuffleModeEnabled,
            isPlaying = true,
            isSingleTrack = currentPlaylist.size == 1,
            playingTrackIndex = currentIndex
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

    private inner class PlayerEventListener : Player.Listener {
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

            val currentIndex = exoPlayer.currentMediaItemIndex
            playerInfoFlow.update { current ->
                current?.copy(
                    trackInfo = currentPlaylist[currentIndex],
                    playingTrackIndex = currentIndex
                )
            }

            startProgressUpdater()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            playerInfoFlow.update { current ->
                current?.copy(
                    isPlaying = exoPlayer.playWhenReady,
                )
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (progressJob?.isActive == false) {
                currentProgressMlsFlow.update { newPosition.positionMs }
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                playerInfoFlow.update { current ->
                    current?.copy(
                        playingTrackIndex = exoPlayer.currentMediaItemIndex,
                    )
                }
            }
        }
    }
}

