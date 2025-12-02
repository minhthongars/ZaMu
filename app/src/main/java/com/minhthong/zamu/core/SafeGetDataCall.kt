package com.minhthong.zamu.core

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
            val errorMessage = e.mapToAppError().errorMessage
            Result.Error(errorMessage)
        }
    }
}