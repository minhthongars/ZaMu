package com.minhthong.core

import android.database.sqlite.SQLiteException
import android.net.http.HttpException
import androidx.annotation.StringRes
import org.json.JSONException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

sealed class AppError(
    @StringRes val errorResId: Int,
    open val cause: Throwable? = null
) {
    data class NetworkError(override val cause: Throwable? = null)
        : AppError(R.string.error_network, cause)

    data class TimeoutError(override val cause: Throwable? = null)
        : AppError(R.string.error_timeout, cause)

    data class ServerError(override val cause: Throwable? = null)
        : AppError(R.string.error_server, cause)

    data class DatabaseError(override val cause: Throwable? = null)
        : AppError(R.string.error_database, cause)

    data class AuthError(override val cause: Throwable? = null)
        : AppError(R.string.error_auth, cause)

    data class ParseError(override val cause: Throwable? = null)
        : AppError(R.string.error_parse, cause)

    data class UnknownError(override val cause: Throwable? = null)
        : AppError(R.string.error_unknown, cause)
}


fun Throwable.toAppError(): AppError = when (this) {
    is UnknownHostException,
    is ConnectException,
    is SocketException -> AppError.NetworkError(this)

    is SocketTimeoutException -> AppError.TimeoutError(this)

    is HttpException -> AppError.ServerError(this)

    is SSLException -> AppError.NetworkError(this)

    is SQLiteException -> AppError.DatabaseError(this)

    is JSONException -> AppError.ParseError(this)

    else -> AppError.UnknownError(this)
}
