package com.minhthong.core

sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(val messageId: Int) : Result<Nothing>()
}

suspend fun <T> Result<T>.onSuccess(
    onSuccess: suspend (T) -> Unit,
): Result<T> {
    if (this is Result.Success) {
        onSuccess.invoke(this.data)
    }
    return this
}

fun <T> Result<T>.onError(
    onError: (Int) -> Unit,
): Result<T> {
    if (this is Result.Error) {
        onError.invoke(this.messageId)
    }
    return this
}

