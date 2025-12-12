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
            name = entity.title,
            avatar = Utils.getAlbumArt(context, entity.uri),
            performer = entity.artist,
            trackId = entity.id,
            isPlaying = false,
            isRemoving = false
        )
    }

    fun List<PlaylistItemEntity>.toPresentation(): List<PlaylistUiState.Track> {
        return map { it.toPresentation() }
    }
}