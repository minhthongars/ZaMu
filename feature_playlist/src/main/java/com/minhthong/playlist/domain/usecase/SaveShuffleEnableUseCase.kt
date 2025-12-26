package com.minhthong.playlist.domain.usecase

import com.minhthong.core.common.Result
import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class SaveShuffleEnableUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(isEnable: Boolean): Result<Boolean> {
        return repository.setShuffleEnable(isEnable)
    }
}