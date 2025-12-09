package com.minhthong.playlist.domain

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylist(): Flow<List<TrackEntity>>

    fun observerTrackInPlaylist(trackId: Long): Flow<Boolean>

    suspend fun insertTrackToPlaylist(trackEntity: TrackEntity): Result<Unit>

    suspend fun removeTrackFromPlaylist(trackId: Long): Result<Unit>

    suspend fun updateTrackOrder(trackId: Long, newOrder: Int): Result<Unit>
}