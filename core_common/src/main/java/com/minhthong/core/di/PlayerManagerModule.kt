package com.minhthong.core.di

import android.content.Context
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.player.PlayerManagerImpl
import com.minhthong.core.player.AudioFocusManager
import com.minhthong.core.transformer.TransformerWrapper
import com.minhthong.core.transformer.TransformerWrapperImpl
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
        context: Context
    ): PlayerManager {
        return PlayerManagerImpl(
            mainDispatcher = mainDispatcher,
            audioFocusManager = AudioFocusManager(context),
            context = context
        )
    }

    @Singleton
    @Provides
    fun provideTransformerWrapper(
        context: Context,
        @MainDispatcher mainDispatcher: CoroutineDispatcher,
    ): TransformerWrapper {
        return TransformerWrapperImpl(context, mainDispatcher)
    }
}