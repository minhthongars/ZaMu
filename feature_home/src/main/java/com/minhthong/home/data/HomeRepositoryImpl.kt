package com.minhthong.home.data

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.safeGetDataCall
import com.minhthong.home.data.datasource.DeviceDataSource
import com.minhthong.home.data.datasource.RemoteDataSource
import com.minhthong.home.data.mapper.DataToDomainMapper
import com.minhthong.home.domain.HomeRepository
import com.minhthong.home.domain.model.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay

class HomeRepositoryImpl(
    private val deviceDataSource: DeviceDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val mapper: DataToDomainMapper,
    private val ioDispatcher: CoroutineDispatcher,
): HomeRepository {

    override suspend fun getTrackFromDeviceStore(): Result<List<TrackEntity>> {
        return safeGetDataCall(
            dispatcher = ioDispatcher,
            getDataCall = {
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
                delay(1000)
                val response = remoteDataSource.fetchUserData()
                with(mapper) { response.toDomain() }
            }
        )
    }
}