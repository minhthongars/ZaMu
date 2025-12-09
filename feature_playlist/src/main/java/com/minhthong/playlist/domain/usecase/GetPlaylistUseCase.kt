package com.minhthong.playlist.domain.usecase

import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.domain.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    operator fun invoke(): Flow<List<TrackEntity>> {
        return repository.getPlaylist()
    }
}