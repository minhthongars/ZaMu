package com.minhthong.core.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.PlayerEntity
import com.minhthong.core.model.TrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlayerManager {

    val hasSetPlaylistFlow: Flow<Boolean>

    val currentProgressMlsFlow: StateFlow<Long>

    val playerInfoFlow: StateFlow<PlayerEntity?>

    fun initialize(context: Context)

    fun release()

    fun play()

    fun loop()

    fun shuffle()

    fun moveToNext()

    fun moveToPrevious()

    fun setPlaylist(tracks: List<TrackEntity>, startIndex: Int)

    fun seek(positionMs: Long)

    fun getPlayer(): ExoPlayer
}