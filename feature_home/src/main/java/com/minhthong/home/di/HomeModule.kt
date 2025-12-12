package com.minhthong.home.di

import android.content.Context
import com.minhthong.core.di.DefaultDispatcher
import com.minhthong.core.di.IoDispatcher
import com.minhthong.home.data.HomeRepositoryImpl
import com.minhthong.home.data.datasource.DeviceDataSource
import com.minhthong.home.data.datasource.RemoteDataSource
import com.minhthong.home.data.mapper.DataToDomainMapper
import com.minhthong.home.domain.HomeRepository
import com.minhthong.home.presentation.mapper.EntityToPresentationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideContentResolverDataSource(
        context: Context
    ): DeviceDataSource {
        return DeviceDataSource(
            contentResolver = context.contentResolver
        )
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(): RemoteDataSource {
        return RemoteDataSource()
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        dataSource: DeviceDataSource,
        remoteDataSource: RemoteDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): HomeRepository {
        return HomeRepositoryImpl(
            deviceDataSource = dataSource,
            mapper = DataToDomainMapper(),
            remoteDataSource = remoteDataSource,
            ioDispatcher = ioDispatcher,
        )
    }

    @Provides
    fun provideHomeEntityToPresentationMapper(
        context: Context
    ): EntityToPresentationMapper {
        return EntityToPresentationMapper(context)
    }
}