package com.minhthong.setting.presentation

import android.graphics.Bitmap

data class CutAdapterItem(
    val id: Int,
    val name: String,
    val avatar: Bitmap?,
    val cutInfo: String,
    val order: Int?
)