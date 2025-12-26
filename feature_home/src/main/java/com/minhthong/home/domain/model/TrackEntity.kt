package com.minhthong.home.domain.model

import android.graphics.Bitmap
import android.net.Uri

data class TrackEntity(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val uri: Uri,
    val avatarBitmap: Bitmap?
)