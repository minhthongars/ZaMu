package com.minhthong.playlist_feature_api

import android.graphics.Bitmap
import com.minhthong.core.common.Result
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistApi {
    suspend fun addTrackToPlaylistAwareShuffle(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        avatarBitmap: Bitmap?
    ): Result<PlaylistItemEntity>

    fun observerPlaylist(): Flow<List<PlaylistItemEntity>>
}