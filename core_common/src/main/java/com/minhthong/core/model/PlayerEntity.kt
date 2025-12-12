package com.minhthong.core.model

data class PlayerEntity(
    val trackInfo: PlaylistItemEntity,
    val isPlaying: Boolean,
    val isLooping: Boolean,
    val isShuffling: Boolean,
)