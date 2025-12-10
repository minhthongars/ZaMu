package com.minhthong.playlist.domain.model

import com.minhthong.core.model.TrackEntity

data class PlaylistItemEntity(
    val id: Int,
    val entity: TrackEntity
)