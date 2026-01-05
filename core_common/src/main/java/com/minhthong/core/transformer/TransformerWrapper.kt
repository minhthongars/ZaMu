package com.minhthong.core.transformer

import android.net.Uri

interface TransformerWrapper {
    suspend fun cutAudio(
        startMls: Long,
        endMls: Long,
        uri: Uri,
        onProgressChange: (Int) -> Unit
    ): String

    suspend fun createMashup(
        uriList: List<Uri>,
        onProgressChange: (Int) -> Unit,
    ): String
}