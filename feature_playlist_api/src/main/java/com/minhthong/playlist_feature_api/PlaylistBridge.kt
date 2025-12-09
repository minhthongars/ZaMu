package com.minhthong.playlist_feature_api

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistBridge {
    suspend fun addTrackToPlaylist(trackEntity: TrackEntity): Result<Unit>

    suspend fun observerTrackInPlaylist(trackId: Long): Flow<Boolean>
}