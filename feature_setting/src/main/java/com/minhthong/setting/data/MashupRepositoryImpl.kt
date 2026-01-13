package com.minhthong.setting.data

import androidx.core.net.toUri
import com.minhthong.core.common.Result
import com.minhthong.core.common.safeGetDataCall
import com.minhthong.core.util.Utils.toBitmap
import com.minhthong.feature_mashup_api.entity.CutEntity
import com.minhthong.feature_mashup_api.repository.MashupRepository
import com.minhthong.playlist_feature_api.PlaylistApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MashupRepositoryImpl(
    private val mashupDao: MashupDao,
    private val ioDispatcher: CoroutineDispatcher
): MashupRepository {

    override fun getAllCuts(): Flow<List<CutEntity>> {
        return mashupDao.getAllCuts().map { cuts ->
            cuts.map { dto ->
                CutEntity(
                    id = dto.id,
                    uri = dto.uriString.toUri(),
                    name = dto.name,
                    performer = dto.performer,
                    startPosition = dto.startPosition,
                    endPosition = dto.endPosition,
                    duration = dto.duration,
                    avatars = emptyList(),
                    parentTracks = dto.parentTrackId
                )
            }
        }
    }

    override suspend fun insertCut(
        uriString: String,
        name: String,
        performer: String,
        duration: Long,
        startPosition: Long,
        endPosition: Long,
        parentTrackId: List<Long>,
    ): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                mashupDao.insertCut(
                    CutDto(
                        uriString = uriString,
                        name = name,
                        performer = performer,
                        duration = duration,
                        startPosition = startPosition,
                        endPosition = endPosition,
                        parentTrackId = parentTrackId
                    )
                )
            }
        )
    }

    override suspend fun removeCut(id: Long): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                mashupDao.deleteCut(cutId = id)
            }
        )
    }
}