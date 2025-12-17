package com.minhthong.playlist.presentaion.mapper

import android.content.Context
import com.minhthong.core.util.Utils
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.playlist.presentaion.PlaylistUiState

class PresentationMapper(
    private val context: Context
) {

    private suspend fun PlaylistItemEntity.toPresentation(): PlaylistUiState.Track {
        return PlaylistUiState.Track(
            id = id,
            name = title,
            avatar = getAvatarBitmap(context),
            performer = artist,
            trackId = trackId,
            isPlaying = false,
            isRemoving = false
        )
    }

    suspend fun List<PlaylistItemEntity>.toPresentation(): List<PlaylistUiState.Track> {
        return map { it.toPresentation() }
    }
}