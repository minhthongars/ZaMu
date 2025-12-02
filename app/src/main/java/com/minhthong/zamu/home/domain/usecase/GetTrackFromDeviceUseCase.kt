package com.minhthong.zamu.home.domain.usecase

import com.minhthong.zamu.core.Result
import com.minhthong.zamu.home.domain.HomeRepository
import com.minhthong.zamu.home.domain.model.TrackEntity
import javax.inject.Inject

class GetTrackFromDeviceUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): Result<List<TrackEntity>> {
        return homeRepository.getTrackFromDeviceStore()
    }
}