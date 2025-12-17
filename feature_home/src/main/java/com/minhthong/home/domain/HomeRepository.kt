package com.minhthong.home.domain

import com.minhthong.home.domain.model.TrackEntity
import com.minhthong.core.Result
import com.minhthong.home.domain.model.RemoteTrackEntity
import com.minhthong.home.domain.model.UserEntity

interface HomeRepository {
    suspend fun getTrackFromDeviceStore(): Result<List<TrackEntity>>

    suspend fun fetchUserInfo(): Result<UserEntity>

    suspend fun fetchPremiumTrack(): Result<List<RemoteTrackEntity>>
}