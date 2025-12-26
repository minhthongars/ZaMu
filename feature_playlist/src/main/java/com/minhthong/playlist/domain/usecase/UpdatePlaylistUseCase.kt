package com.minhthong.playlist.domain.usecase

import com.minhthong.core.common.Result
import com.minhthong.playlist.domain.PlaylistRepository
import com.minhthong.core.model.PlaylistItemEntity
import javax.inject.Inject

class UpdatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(isShuffle: Boolean, tracks: List<PlaylistItemEntity>): Result<Unit> {
        return repository.updatePlaylist(isShuffle, tracks)
    }
}