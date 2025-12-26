package com.minhthong.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T> safeGetDataCall(
    dispatcher: CoroutineDispatcher,
    getDataCall: suspend () -> T,
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(getDataCall())
        } catch (e: Exception) {
            val errorMessageId = e.toAppError().errorResId
            Result.Error(errorMessageId)
        }
    }
}