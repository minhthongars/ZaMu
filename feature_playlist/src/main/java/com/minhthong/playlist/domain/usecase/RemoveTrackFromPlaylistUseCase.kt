package com.minhthong.playlist.domain.usecase

import com.minhthong.core.common.Result
import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class RemoveTrackFromPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(playlistItemId: Int): Result<Unit> {
        return repository.removeTrackFromPlaylist(playlistItemId)
    }
}