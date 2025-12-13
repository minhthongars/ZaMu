package com.minhthong.playlist_feature_api

import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.model.TrackEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistBridge {
    suspend fun addTrackToPlaylistAwareShuffle(trackEntity: TrackEntity): Result<PlaylistItemEntity>

    suspend fun observerTrackInPlaylist(trackId: Long): Flow<Boolean>

    fun observerPlaylist(): Flow<List<PlaylistItemEntity>>
}