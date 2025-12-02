package com.minhthong.zamu.home.domain

import com.minhthong.zamu.core.Result
import com.minhthong.zamu.home.domain.model.TrackEntity
import com.minhthong.zamu.home.domain.model.UserEntity

interface HomeRepository {

    suspend fun getTrackFromDeviceStore(): Result<List<TrackEntity>>

    suspend fun fetchUserInfo(): Result<UserEntity>
}