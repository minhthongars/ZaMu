package com.minhthong.zamu.core

import android.database.sqlite.SQLiteException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class AppError(
    private val message: String,
    private val cause: Throwable? = null
) {

    val errorMessage get() = "$message: ${cause?.message}"

    class NetworkError(cause: Throwable? = null) : AppError("Network error occurred", cause)
    class TimeoutError(cause: Throwable? = null) : AppError("Request timed out", cause)
    class ServerError(cause: Throwable? = null) : AppError("Server error occurred", cause)
    class DatabaseError(cause: Throwable? = null) : AppError("Database operation failed", cause)
    class UnknownError(cause: Throwable? = null) : AppError("An unknown error occurred", cause)
}

fun Throwable.mapToAppError(): AppError {
    return when (this) {
        is UnknownHostException -> AppError.NetworkError(this)
        is SocketTimeoutException -> AppError.TimeoutError(this)
        is SQLiteException -> AppError.DatabaseError(this)
        else -> AppError.UnknownError(this)
    }
}