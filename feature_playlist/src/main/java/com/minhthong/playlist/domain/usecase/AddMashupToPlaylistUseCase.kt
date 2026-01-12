package com.minhthong.playlist.domain.usecase

import com.minhthong.core.common.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class AddMashupToPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        parentTrackId: List<Long>
    ): Result<PlaylistItemEntity> {
        return repository.insertMashupToPlaylist(
            trackId,
            title,
            performer,
            uri,
            parentTrackId
        )
    }
}