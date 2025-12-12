package com.minhthong.playlist.domain.usecase

import com.minhthong.core.getData
import com.minhthong.playlist.domain.PlaylistRepository
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPlaylistAwareShuffleUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    operator fun invoke(): Flow<List<PlaylistItemEntity>> {
        return repository.getPlaylist().map { playlistItemEntities ->
            val isShuffle = repository.getIsShuffleEnable().getData() ?: false
            if (isShuffle) {
                playlistItemEntities.sortedBy { it.shuffleOrderIndex }
            } else {
                playlistItemEntities.sortedBy { it.orderIndex }
            }
        }
    }
}