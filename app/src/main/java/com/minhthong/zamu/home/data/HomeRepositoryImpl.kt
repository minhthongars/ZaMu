package com.minhthong.zamu.home.data

import com.minhthong.zamu.core.Result
import com.minhthong.zamu.core.safeGetDataCall
import com.minhthong.zamu.home.data.datasource.DeviceDataSource
import com.minhthong.zamu.home.data.datasource.RemoteDataSource
import com.minhthong.zamu.home.data.mapper.DataToDomainMapper
import com.minhthong.zamu.home.domain.HomeRepository
import com.minhthong.zamu.home.domain.model.TrackEntity
import com.minhthong.zamu.home.domain.model.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay

class HomeRepositoryImpl(
    private val deviceDataSource: DeviceDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val mapper: DataToDomainMapper,
    private val ioDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher,
): HomeRepository {

    override suspend fun getTrackFromDeviceStore(): Result<List<TrackEntity>> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                delay(500)
                deviceDataSource.getTracksFromDevice().map { trackDto ->
                    with(mapper) { trackDto.toDomain() }
                }
            }
        )
    }

    override suspend fun fetchUserInfo(): Result<UserEntity> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
                delay(1000L)
                val response = remoteDataSource.fetchUserData()
                with(mapper) { response.toDomain() }
            }
        )
    }
}