package com.minhthong.core.player

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.ControllerState
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.StateFlow

interface PlayerManager {

    val currentProgressMlsFlow: StateFlow<Long>

    val currentBufferMlsFlow: StateFlow<Long>

    val controllerInfoFlow: StateFlow<ControllerState?>

    fun initialize()

    fun setPlaylist(playlistItems: List<PlaylistItemEntity>)

    fun release()

    fun seekToMediaItem(playlistItemId: Int)

    fun seekToLastMediaItem(playlistItem: PlaylistItemEntity)

    fun playOrPause()

    fun seek(positionMs: Long)

    fun loopOrNot()

    fun moveToNext()

    fun moveToPrevious()

    fun getPlayer(): ExoPlayer
}