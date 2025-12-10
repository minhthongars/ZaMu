package com.minhthong.core.model

data class PlayerEntity(
    val trackInfo: TrackEntity,
    val isPlaying: Boolean,
    val isLooping: Boolean,
    val isShuffling: Boolean,
    val isSingleTrack: Boolean
)