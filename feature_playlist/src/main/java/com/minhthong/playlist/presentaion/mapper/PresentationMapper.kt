package com.minhthong.playlist.presentaion.mapper

import android.content.Context
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.util.Utils
import com.minhthong.playlist.presentaion.PlaylistUiState

class PresentationMapper(
    private val context: Context
) {

    fun TrackEntity.toPresentation(): PlaylistUiState.Track {
        return PlaylistUiState.Track(
            id = id,
            name = title,
            avatar = Utils.getAlbumArt(context, uri),
            performer = artist
        )
    }

    fun List<TrackEntity>.toPresentation(): List<PlaylistUiState.Track> {
        return map { it.toPresentation() }
    }
}