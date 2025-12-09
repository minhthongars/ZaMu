package com.minhthong.playlist.domain.usecase

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.domain.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddTrackToPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(trackEntity: TrackEntity): Result<Unit> {
        return repository.insertTrackToPlaylist(trackEntity)
    }
}