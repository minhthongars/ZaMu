package com.minhthong.setting.di

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.room.Room
import com.minhthong.core.di.IoDispatcher
import com.minhthong.feature_mashup_api.repository.MashupRepository
import com.minhthong.setting.data.MashupDao
import com.minhthong.setting.data.MashupDatabase
import com.minhthong.setting.data.MashupRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MashupModule {

    @Provides
    @Singleton
    fun provideDatabase(
        context: Context
    ): MashupDatabase {
        return Room.databaseBuilder(
            context,
            MashupDatabase::class.java,
            "mashup"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDao(
        playlistDb: MashupDatabase
    ): MashupDao {
        return playlistDb.dao
    }

    @Provides
    @Singleton
    fun provideRepository(
        dao: MashupDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MashupRepository {
        return MashupRepositoryImpl(
            mashupDao = dao,
            ioDispatcher = ioDispatcher,
        )
    }
}