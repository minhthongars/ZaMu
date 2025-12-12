package com.minhthong.playlist.presentaion

import android.graphics.Bitmap

sealed class PlaylistUiState {

    object Loading: PlaylistUiState()

    data class Error(val messageId: Int): PlaylistUiState()

    data class Success(
        val tracks: List<Track>,
        val isShuffling: Boolean
    ): PlaylistUiState()

    data class Track(
        val id: Int,
        val trackId: Long,
        val avatar: Bitmap?,
        val name: String,
        val performer: String,
        val isPlaying: Boolean,
        val isRemoving: Boolean
    )
}