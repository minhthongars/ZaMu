package com.minhthong.feature_mashup_api

import android.graphics.Bitmap
import android.net.Uri

data class CutEntity(
    val id: Int,
    val name: String,
    val performer: String,
    val uri: Uri,
    val duration: Long,
    val startPosition: Long,
    val endPosition: Long,
    val avatar: Bitmap?
)