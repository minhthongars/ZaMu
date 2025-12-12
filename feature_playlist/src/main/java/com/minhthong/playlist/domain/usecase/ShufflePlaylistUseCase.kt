package com.minhthong.playlist.domain.usecase

import com.minhthong.core.Result
import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class ShufflePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(isEnableShuffle: Boolean): Result<Unit> {
        return repository.shufflePlaylist(isEnableShuffle)
    }
}