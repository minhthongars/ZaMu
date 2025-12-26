package com.minhthong.setting.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.minhthong.core.Result
import com.minhthong.core.safeGetDataCall
import com.minhthong.core.util.Utils.toBitmap
import com.minhthong.core.util.Utils.toByteArray
import com.minhthong.feature_mashup_api.CutEntity
import com.minhthong.feature_mashup_api.MashupRepository
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
                    avatar = dto.avatarImage.toBitmap()
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
        avatarBitmap: Bitmap?
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
                        avatarImage = avatarBitmap.toByteArray()
                    )
                )
            }
        )
    }

    override suspend fun removeCut(id: Int): Result<Unit> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
               mashupDao.deleteCut(cutId = id)
            }
        )
    }
}