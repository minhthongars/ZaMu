package com.minhthong.playlist.domain.usecase

import com.minhthong.core.Result
import com.minhthong.playlist.domain.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShuffleEnableUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(): Result<Boolean> {
        return repository.getIsShuffleEnable()
    }
}