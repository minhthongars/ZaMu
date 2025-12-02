package com.minhthong.zamu.home.domain.usecase

import com.minhthong.zamu.core.Result
import com.minhthong.zamu.home.domain.HomeRepository
import com.minhthong.zamu.home.domain.model.UserEntity
import javax.inject.Inject

class FetchUserInfoUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {

    suspend operator fun invoke(): Result<UserEntity> {
        return homeRepository.fetchUserInfo()
    }
}