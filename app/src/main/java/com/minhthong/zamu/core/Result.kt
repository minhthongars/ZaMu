package com.minhthong.zamu.core

sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(val message: String) : Result<Nothing>()
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
    onError: (String) -> Unit,
): Result<T> {
    if (this is Result.Error) {
        onError.invoke(this.message)
    }
    return this
}

