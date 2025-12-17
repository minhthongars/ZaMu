package com.minhthong.playlist.presentaion.mapper

import android.content.Context
import com.minhthong.core.util.Utils
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.playlist.presentaion.PlaylistUiState

class PresentationMapper(
    private val context: Context
) {

    private fun PlaylistItemEntity.toPresentation(): PlaylistUiState.Track {
        return PlaylistUiState.Track(
            id = id,
            name = title,
            avatar = Utils.getAlbumArt(context, uri),
            performer = artist,
            trackId = trackId,
            isPlaying = false,
            isRemoving = false
        )
    }

    fun List<PlaylistItemEntity>.toPresentation(): List<PlaylistUiState.Track> {
        return map { it.toPresentation() }
    }
}