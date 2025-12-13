package com.minhthong.home.data.model

import android.net.Uri

data class TrackDto(
    val id: Long?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
    val sizeBytes: Long?,
    val uri: Uri?,
)