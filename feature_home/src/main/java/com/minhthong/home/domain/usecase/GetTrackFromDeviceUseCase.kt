package com.minhthong.home.domain.usecase

import com.minhthong.core.common.Result
import com.minhthong.home.domain.model.TrackEntity
import com.minhthong.home.domain.HomeRepository
import javax.inject.Inject

class GetTrackFromDeviceUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): Result<List<TrackEntity>> {
        return homeRepository.getTrackFromDeviceStore()
    }
}