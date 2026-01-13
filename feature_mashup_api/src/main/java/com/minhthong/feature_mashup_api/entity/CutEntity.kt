package com.minhthong.feature_mashup_api.entity

import android.graphics.Bitmap
import android.net.Uri

data class CutEntity(
    val id: Long,
    val parentTracks: List<Long>,
    val name: String,
    val performer: String,
    val uri: Uri,
    val duration: Long,
    val startPosition: Long,
    val endPosition: Long,
    val avatars: List<Bitmap?>
)