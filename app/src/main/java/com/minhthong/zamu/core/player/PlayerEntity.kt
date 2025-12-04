package com.minhthong.zamu.core.player

import com.minhthong.zamu.home.domain.model.TrackEntity

data class PlayerEntity(
    val trackInfo: TrackEntity,
    val isPlaying: Boolean,
    val isLooping: Boolean,
    val isShuffling: Boolean,
)