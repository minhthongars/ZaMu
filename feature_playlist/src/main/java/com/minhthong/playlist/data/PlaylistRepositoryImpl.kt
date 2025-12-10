package com.minhthong.playlist.data

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.safeGetDataCall
import com.minhthong.playlist.data.dao.PlaylistDao
import com.minhthong.playlist.data.mapper.Mapper.toData
import com.minhthong.playlist.data.mapper.Mapper.toDomain
import com.minhthong.playlist.domain.PlaylistRepository
import com.minhthong.playlist.domain.model.PlaylistItemEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val dao: PlaylistDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val gapOrder: Int = Constant.GAP_ORDER
): PlaylistRepository {


    override fun getPlaylist(): Flow<List<PlaylistItemEntity>> {
        val playlist = dao.getPlaylist().map { playlist ->
            playlist.sortedBy { it.orderIndex }
        }
        return playlist.map { it.toDomain() }
    }

    override fun observerTrackInPlaylist(trackId: Long): Flow<Boolean> {
        return dao.observeTrackInPlaylist(trackId)
    }

    override suspend fun insertTrackToPlaylist(trackEntity: TrackEntity): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                val newOrder = dao.getNextOrderIndex() + gapOrder
                dao.insertTrack(
                    track = trackEntity.toData().copy(
                        orderIndex = newOrder
                    )
                )
            }
        )
    }

    override suspend fun removeTrackFromPlaylist(playlistItemId: Int): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                dao.deleteTrackById(playlistItemId = playlistItemId)
            }
        )
    }

    override suspend fun updateTrackOrder(trackId: Long, newOrder: Int): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {

            }
        )
    }
}