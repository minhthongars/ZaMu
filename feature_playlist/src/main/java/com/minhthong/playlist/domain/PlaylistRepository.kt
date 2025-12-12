package com.minhthong.playlist.domain

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylist(): Flow<List<PlaylistItemEntity>>

    fun observerTrackInPlaylist(trackId: Long): Flow<Boolean>

    suspend fun insertTrackToPlaylist(isShuffle: Boolean, trackEntity: TrackEntity): Result<Unit>

    suspend fun removeTrackFromPlaylist(playlistItemId: Int): Result<Unit>

    suspend fun updatePlaylist(isShuffle: Boolean, tracks: List<PlaylistItemEntity>): Result<Unit>

    suspend fun getIsShuffleEnable(): Result<Boolean>

    suspend fun setShuffleEnable(isEnable: Boolean): Result<Unit>

    suspend fun shufflePlaylist(isShuffle: Boolean): Result<Unit>
}