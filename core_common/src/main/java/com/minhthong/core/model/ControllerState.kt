package com.minhthong.core.model

data class ControllerState(
    val isPlaying: Boolean,
    val isLooping: Boolean,
    val duration: Long,
    val playingItem: PlaylistItemEntity
)