package com.minhthong.setting.presentation

import android.graphics.Bitmap

data class CutAdapterItem(
    val id: Long,
    val name: String,
    val avatar: List<Bitmap?>,
    val cutInfo: String,
    val order: Int?
)