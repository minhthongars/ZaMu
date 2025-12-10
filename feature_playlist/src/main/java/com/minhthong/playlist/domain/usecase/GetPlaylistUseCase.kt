package com.minhthong.playlist.domain.usecase

import com.minhthong.playlist.domain.PlaylistRepository
import com.minhthong.playlist.domain.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    operator fun invoke(): Flow<List<PlaylistItemEntity>> {
        return repository.getPlaylist()
    }
}