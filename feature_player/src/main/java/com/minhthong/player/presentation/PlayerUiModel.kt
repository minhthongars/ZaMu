package com.minhthong.player.presentation

import android.graphics.Bitmap

data class PlayerUiModel(
    val trackName: String = "",
    val performer: String = "",
    val avatar: Bitmap? = null,
    val album: String = "",
    val duration: String = "00:00",
    val playIcon: Int = 0,
    val loopIconColor: Int = 0,
    val shuffleIconColor: Int = 0,
    val sliderBarValue: Long = 0,
    val seekMediaEnable: Boolean = false
)