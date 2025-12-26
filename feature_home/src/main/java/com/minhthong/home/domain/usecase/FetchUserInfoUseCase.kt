package com.minhthong.home.domain.usecase

import com.minhthong.core.common.Result
import com.minhthong.home.domain.HomeRepository
import com.minhthong.home.domain.model.UserEntity
import javax.inject.Inject

class FetchUserInfoUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {

    suspend operator fun invoke(): Result<UserEntity> {
        return homeRepository.fetchUserInfo()
    }
}