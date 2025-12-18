package com.minhthong.core.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.ControllerState
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlayerManager {

    val currentProgressMlsFlow: StateFlow<Long>

    val currentBufferMlsFlow: StateFlow<Long>

    val controllerInfoFlow: StateFlow<ControllerState?>

    fun initialize(context: Context)

    fun setPlaylist(playlistItems: List<PlaylistItemEntity>)

    fun release()

    fun seekToMediaItem(playlistItemId: Int)

    fun seekToLastMediaItem(playlistItem: PlaylistItemEntity)

    fun play()

    fun seek(positionMs: Long)

    fun loop()

    fun moveToNext()

    fun moveToPrevious()

    fun getPlayer(): ExoPlayer
}