package com.minhthong.core.di

import com.minhthong.core.player.PlayerManager
import com.minhthong.core.player.PlayerManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PlayerManagerModule {

    @Provides
    @Singleton
    fun providePlayerManager(
        @MainDispatcher mainDispatcher: CoroutineDispatcher,
    ): PlayerManager {
        return PlayerManagerImpl(
            mainDispatcher = mainDispatcher,
        )
    }
}