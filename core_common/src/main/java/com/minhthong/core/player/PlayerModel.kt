package com.minhthong.core.player

import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

open class PlayerModel {
    protected lateinit var exoPlayer: ExoPlayer

    protected lateinit var playerScope: CoroutineScope

    protected var updatePlayingPositionJob: Job? = null

    protected var updateBufferPositionJob: Job? = null

    protected var currentPlaylistItems = emptyList<PlaylistItemEntity>()

    protected var currentItemIndex = -1

    protected var isLooping = false
}