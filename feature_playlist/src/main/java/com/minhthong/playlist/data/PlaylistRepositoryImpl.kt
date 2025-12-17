package com.minhthong.playlist.data

import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.safeGetDataCall
import com.minhthong.playlist.data.dao.PlaylistDao
import com.minhthong.playlist.data.mapper.Mapper.toData
import com.minhthong.playlist.data.mapper.Mapper.toDomain
import com.minhthong.playlist.data.model.TrackDto
import com.minhthong.playlist.data.sharePref.ShuffleSharePreference
import com.minhthong.playlist.domain.PlaylistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class PlaylistRepositoryImpl(
    private val dao: PlaylistDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val shuffleSharedPreferences: ShuffleSharePreference,
    private val gapOrder: Long = DbConstant.GAP_ORDER
): PlaylistRepository {

    override fun getPlaylist(): Flow<List<PlaylistItemEntity>> {
        val playlist = dao.getPlaylist()
        return playlist.map { dto -> dto.toDomain() }
    }

    override suspend fun insertTrackToPlaylist(
        trackId: Long,
        title: String,
        performer: String,
        uri: String
    ): Result<PlaylistItemEntity> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                val newOrder = dao.getNextOrderIndex() + gapOrder
                val newShuffleOrder = dao.shuffleOrderIndex() + gapOrder

                val dto = TrackDto(
                    trackId = trackId,
                    title = title,
                    artist = performer,
                    uri = uri,
                    orderIndex = newOrder,
                    shuffleOrderIndex = newShuffleOrder
                )

                dao.insertTrack(dto)

                val insertedTrack = dao.getTrackByOrder(order = newOrder)
                    ?: throw IllegalStateException("Track not found after insert")

                insertedTrack.toDomain()
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

    override suspend fun updatePlaylist(
        isShuffle: Boolean,
        tracks: List<PlaylistItemEntity>
    ): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                val tracksDto = tracks.mapIndexed { index, entity ->
                    val dto = entity.toData()
                    if (isShuffle) {
                        dto.copy(shuffleOrderIndex = index * gapOrder)
                    } else {
                        dto.copy(orderIndex = index * gapOrder)
                    }
                }
                dao.upsertTracks(tracks = tracksDto)
            }
        )
    }

    override suspend fun getIsShuffleEnable(): Result<Boolean> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                shuffleSharedPreferences.getIsEnable()
            }
        )
    }

    override suspend fun setShuffleEnable(isEnable: Boolean): Result<Boolean> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                shuffleSharedPreferences.setIsEnable(isEnable)
            }
        )
    }

    override suspend fun shufflePlaylist(isShuffle: Boolean): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                val trackDtoList = dao.getPlaylist().first()
                val newList = if (isShuffle) {
                    trackDtoList.map { dto ->
                        val shuffleIndex = Random.nextLong(0, 10_000)
                        dto.copy(shuffleOrderIndex = shuffleIndex)
                    }
                } else {
                    trackDtoList.map { dto ->
                        dto.copy(shuffleOrderIndex = dto.orderIndex)
                    }
                }

                dao.upsertTracks(newList)
            }
        )
    }
}