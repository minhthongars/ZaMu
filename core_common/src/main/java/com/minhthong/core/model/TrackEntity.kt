package com.minhthong.core.model

import android.net.Uri

data class TrackEntity(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val uri: Uri
)