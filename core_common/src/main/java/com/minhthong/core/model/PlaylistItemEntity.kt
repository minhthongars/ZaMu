package com.minhthong.core.model

data class PlaylistItemEntity(
    val id: Int,
    val orderIndex: Long,
    val shuffleOrderIndex: Long,
    val isPlaying: Boolean,
    val entity: TrackEntity
)