package com.minhthong.home.domain.usecase

import com.minhthong.core.Result
import com.minhthong.home.domain.HomeRepository
import com.minhthong.home.domain.model.RemoteTrackEntity
import javax.inject.Inject

class FetchPremiumTrackUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {

    suspend operator fun invoke(): Result<List<RemoteTrackEntity>> {
        return homeRepository.fetchPremiumTrack()
    }
}