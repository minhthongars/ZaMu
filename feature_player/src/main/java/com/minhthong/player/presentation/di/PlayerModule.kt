package com.minhthong.player.presentation.di

import android.content.Context
import com.minhthong.player.presentation.mapper.EntityToPresentationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    fun providePlayerEntityToPresentationMapper(
        context: Context
    ): EntityToPresentationMapper {
        return EntityToPresentationMapper(context)
    }
}