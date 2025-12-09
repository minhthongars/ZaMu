package com.minhthong.playlist.domain.usecase

import com.minhthong.playlist.domain.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserverTrackInPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    operator fun invoke(trackId: Long): Flow<Boolean> {
        return repository.observerTrackInPlaylist(trackId = trackId)
    }
}