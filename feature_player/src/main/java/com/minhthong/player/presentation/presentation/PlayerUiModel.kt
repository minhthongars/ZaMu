package com.minhthong.player.presentation.presentation

import android.graphics.Bitmap

data class PlayerUiModel(
    val trackName: String = "",
    val performer: String = "",
    val avatar: List<Bitmap?> = emptyList(),
    val album: String = "",
    val duration: String = "00:00",
    val playIcon: Int = 0,
    val loopIconColor: Int = 0,
    val shuffleIconColor: Int = 0,
    val sliderBarValue: Long = 0,
    val startAnimation: Boolean = false,
    val isAudioCutting: Boolean = false,
    val playbackSpeed: Float = 1.0f
)