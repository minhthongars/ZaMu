package com.minhthong.zamu.di

import android.content.Context
import com.minhthong.zamu.core.player.PlayerManager
import com.minhthong.zamu.core.player.PlayerManagerImpl
import com.minhthong.zamu.home.data.HomeRepositoryImpl
import com.minhthong.zamu.home.data.datasource.DeviceDataSource
import com.minhthong.zamu.home.data.datasource.RemoteDataSource
import com.minhthong.zamu.home.data.mapper.DataToDomainMapper
import com.minhthong.zamu.home.domain.HomeRepository
import com.minhthong.zamu.home.presentation.mapper.EntityToPresentationMapper
import com.minhthong.zamu.player.presentation.mapper.EntityToPresentationMapper as PlayerMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolverDataSource(
        @ApplicationContext context: Context
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
    fun provideHomeEntityToPresentationMapper(
        @ApplicationContext context: Context
    ): EntityToPresentationMapper {
        return EntityToPresentationMapper(context)
    }

    @Provides
    fun providePlayerEntityToPresentationMapper(
        @ApplicationContext context: Context
    ): PlayerMapper {
        return PlayerMapper(context)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        dataSource: DeviceDataSource,
        remoteDataSource: RemoteDataSource,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): HomeRepository {
        return HomeRepositoryImpl(
            deviceDataSource = dataSource,
            mapper = DataToDomainMapper(),
            remoteDataSource = remoteDataSource,
            ioDispatcher = ioDispatcher,
            defaultDispatcher = defaultDispatcher
        )
    }

    @Provides
    @Singleton
    fun providePlayerManager(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): PlayerManager {
        return PlayerManagerImpl(
            dispatcher = defaultDispatcher,
        )
    }
}