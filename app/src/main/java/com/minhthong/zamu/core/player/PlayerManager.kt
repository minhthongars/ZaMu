package com.minhthong.zamu.core.player

import android.content.Context
import com.minhthong.zamu.home.domain.model.TrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlayerManager {
    fun initialize(context: Context)

    fun setPlaylist(tracks: List<TrackEntity>, startIndex: Int)

    fun hasSetPlaylist(): Flow<Boolean>

    fun release()

    fun playerInfo(): StateFlow<PlayerEntity?>

    fun currentProgressMls(): StateFlow<Long>

    fun play()

    fun seek(positionMs: Long)

    fun loop()

    fun shuffle()

    fun moveToNext()

    fun moveToPrevious()
}