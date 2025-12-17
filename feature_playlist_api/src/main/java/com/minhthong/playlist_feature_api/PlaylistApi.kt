package com.minhthong.playlist_feature_api

import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.model.PlaylistItemEntity.Source
import kotlinx.coroutines.flow.Flow

interface PlaylistApi {
    suspend fun addTrackToPlaylistAwareShuffle(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        source: Source,
        avatarUrl: String?
    ): Result<PlaylistItemEntity>

    fun observerPlaylist(): Flow<List<PlaylistItemEntity>>
}