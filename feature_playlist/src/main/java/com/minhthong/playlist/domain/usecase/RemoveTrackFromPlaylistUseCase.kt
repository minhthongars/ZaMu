package com.minhthong.playlist.domain.usecase

import com.minhthong.core.Result
import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class RemoveTrackFromPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(trackId: Long): Result<Unit> {
        return repository.removeTrackFromPlaylist(trackId)
    }
}