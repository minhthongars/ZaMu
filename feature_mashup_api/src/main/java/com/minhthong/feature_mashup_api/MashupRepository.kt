package com.minhthong.feature_mashup_api

import android.graphics.Bitmap
import com.minhthong.core.Result
import kotlinx.coroutines.flow.Flow

interface MashupRepository {
    fun getAllCuts(): Flow<List<CutEntity>>

    suspend fun insertCut(
        uriString: String,
        name: String,
        performer: String,
        duration: Long,
        startPosition: Long,
        endPosition: Long,
        avatarBitmap: Bitmap?
    ): Result<Unit>

    suspend fun removeCut(id: Int): Result<Unit>
}