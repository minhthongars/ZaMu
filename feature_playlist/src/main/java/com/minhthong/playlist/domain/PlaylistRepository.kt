package com.minhthong.playlist.domain

import android.graphics.Bitmap
import com.minhthong.core.common.Result
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylist(): Flow<List<PlaylistItemEntity>>

    suspend fun insertTrackToPlaylist(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        avatarBitmap: Bitmap?
    ): Result<PlaylistItemEntity>

    suspend fun removeTrackFromPlaylist(playlistItemId: Int): Result<Unit>

    suspend fun updatePlaylist(isShuffle: Boolean, tracks: List<PlaylistItemEntity>): Result<Unit>

    suspend fun getIsShuffleEnable(): Result<Boolean>

    suspend fun setShuffleEnable(isEnable: Boolean): Result<Boolean>

    suspend fun shufflePlaylist(isShuffle: Boolean): Result<Unit>
}