package com.minhthong.playlist.presentaion.mapper

import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.playlist.presentaion.PlaylistUiState

class PresentationMapper {

    private fun PlaylistItemEntity.toPresentation(): PlaylistUiState.Track {
        return PlaylistUiState.Track(
            id = id,
            name = title,
            avatar = avatarImage,
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