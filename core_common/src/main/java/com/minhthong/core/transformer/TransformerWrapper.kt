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

    suspend fun createMashupWithCrossfade(
        uriList: List<Uri>,
        durations: List<Long>,
        crossfadeDurationMs: Long,
        onProgressChange: (Int) -> Unit,
    ): String
}