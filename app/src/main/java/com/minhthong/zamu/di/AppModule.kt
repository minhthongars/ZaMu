package com.minhthong.zamu.di

import android.content.Context
import com.minhthong.core.di.DefaultDispatcher
import com.minhthong.core.di.MainDispatcher
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.player.PlayerManagerImpl
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import com.minhthong.zamu.R
import com.minhthong.zamu.navigation.NavigationImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(
        @ApplicationContext context: Context
    ) = context

    @Provides
    @Singleton
    fun providePlayerManager(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        @MainDispatcher mainDispatcher: CoroutineDispatcher
    ): PlayerManager {
        return PlayerManagerImpl(
            defaultDispatcher = defaultDispatcher,
            mainDispatcher = mainDispatcher
        )
    }

    @Provides
    @Singleton
    fun provideScreenMap(): Map<Screen, Int> {
        return mapOf(
            Screen.HOME to R.id.homeFragment,
            Screen.PLAYER to R.id.playerFragment,
            Screen.PLAYLIST to R.id.playlistFragment,
            Screen.SETTING to R.id.settingsFragment
        )
    }

    @Provides
    @Singleton
    fun provideAppNavigation(
        screenMap: Map<Screen, Int>
    ): Navigation {
        return NavigationImpl(screenMap)
    }
}