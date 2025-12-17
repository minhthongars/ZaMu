package com.minhthong.core.model

import android.net.Uri

data class PlaylistItemEntity(
    val id: Int,
    val orderIndex: Long,
    val shuffleOrderIndex: Long,
    val trackId: Long,
    val title: String,
    val artist: String,
    val uri: Uri
)