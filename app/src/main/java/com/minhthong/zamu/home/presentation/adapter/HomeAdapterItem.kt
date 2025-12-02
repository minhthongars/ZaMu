package com.minhthong.zamu.home.presentation.adapter

import androidx.annotation.DimenRes

sealed class HomeAdapterItem(
    val viewType: Int
) {
    data class UserInfo(
        val name: String = "",
        val tier: String = "",
        val avatarUrl: String = "",
    ): HomeAdapterItem(viewType = ViewType.USER_INFO)

    data class Title(
        val content: String
    ): HomeAdapterItem(viewType = ViewType.TITLE)

    data class LoadingView(
        @DimenRes val viewHeight: Int
    ): HomeAdapterItem(viewType = ViewType.LOADING_VIEW)

    data class ErrorView(
        @DimenRes val viewHeight: Int,
        val type: Int
    ): HomeAdapterItem(viewType = ViewType.ERROR_VIEW)

    data class Track(
        val name: String,
        val performer: String,
        val sizeString: String,
        val durationString: String,
    ): HomeAdapterItem(viewType = ViewType.TRACK)

    // Constant
    object ViewType {
        const val LOADING_VIEW = -1
        const val ERROR_VIEW = -2
        const val USER_INFO = 1
        const val TITLE = 3
        const val TRACK = 5
    }
}