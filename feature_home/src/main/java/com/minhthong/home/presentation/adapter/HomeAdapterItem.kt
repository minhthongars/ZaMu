package com.minhthong.home.presentation.adapter

import android.graphics.Bitmap
import androidx.annotation.DimenRes
import androidx.annotation.StringRes

sealed class HomeAdapterItem(
    val viewType: Int
) {
    abstract fun areItemsTheSame(other: HomeAdapterItem): Boolean

    abstract fun areContentsTheSame(other: HomeAdapterItem): Boolean

    open fun getChangePayload(other: HomeAdapterItem): Any? = null

    data class UserInfo(
        val id: Long,
        val name: String = "",
        val tier: String = "",
        val avatarUrl: String = "",
    ): HomeAdapterItem(viewType = ViewType.USER_INFO) {
        override fun areItemsTheSame(other: HomeAdapterItem): Boolean {
            return other is UserInfo && other.id == id
        }

        override fun areContentsTheSame(other: HomeAdapterItem): Boolean {
            return other is UserInfo && other == this
        }
    }

    data class Title(
        val content: String
    ): HomeAdapterItem(viewType = ViewType.TITLE) {
        override fun areItemsTheSame(other: HomeAdapterItem): Boolean {
            return other is Title && other.content == content
        }

        override fun areContentsTheSame(other: HomeAdapterItem) = true
    }

    data class LoadingView(
        @field:DimenRes val viewHeight: Int
    ): HomeAdapterItem(viewType = ViewType.LOADING_VIEW) {
        override fun areItemsTheSame(other: HomeAdapterItem): Boolean {
            return other is LoadingView && other.viewHeight == viewHeight
        }

        override fun areContentsTheSame(other: HomeAdapterItem) = true
    }

    data class ErrorView(
        @field:DimenRes val viewHeight: Int,
        @field:StringRes val message: Int,
        val type: Int
    ): HomeAdapterItem(viewType = ViewType.ERROR_VIEW) {
        override fun areItemsTheSame(other: HomeAdapterItem): Boolean {
            return other is ErrorView && other.viewHeight == viewHeight
        }

        override fun areContentsTheSame(other: HomeAdapterItem): Boolean {
            return other is ErrorView && other == this
        }
    }

    data class Track(
        val id: Long,
        val name: String,
        val avatarBitmap: Bitmap?,
        val performer: String,
        val sizeString: String,
        val durationString: String,
    ): HomeAdapterItem(viewType = ViewType.TRACK) {
        override fun areItemsTheSame(other: HomeAdapterItem): Boolean {
            return other is Track && other.id == id
        }

        override fun areContentsTheSame(other: HomeAdapterItem): Boolean {
            return other is Track && other == this
        }
    }

    // Constant
    object ViewType {
        const val LOADING_VIEW = -1
        const val ERROR_VIEW = -2
        const val USER_INFO = 1
        const val TITLE = 3
        const val TRACK = 5
    }
}