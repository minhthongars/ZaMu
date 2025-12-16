package com.minhthong.playlist.domain

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylist(): Flow<List<PlaylistItemEntity>>

    suspend fun insertTrackToPlaylist(trackEntity: TrackEntity): Result<PlaylistItemEntity>

    suspend fun removeTrackFromPlaylist(playlistItemId: Int): Result<Unit>

    suspend fun updatePlaylist(isShuffle: Boolean, tracks: List<PlaylistItemEntity>): Result<Unit>

    suspend fun getIsShuffleEnable(): Result<Boolean>

    suspend fun setShuffleEnable(isEnable: Boolean): Result<Boolean>

    suspend fun shufflePlaylist(isShuffle: Boolean): Result<Unit>
}