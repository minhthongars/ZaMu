package com.minhthong.playlist_feature_api

import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.model.TrackEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistApi {
    suspend fun addTrackToPlaylistAwareShuffle(
        trackEntity: TrackEntity
    ): Result<PlaylistItemEntity>

    fun observerPlaylist(): Flow<List<PlaylistItemEntity>>
}