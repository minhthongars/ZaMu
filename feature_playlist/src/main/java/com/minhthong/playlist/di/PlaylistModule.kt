package com.minhthong.playlist.di

import android.content.Context
import androidx.room.Room
import com.minhthong.core.di.IoDispatcher
import com.minhthong.playlist.data.Constant
import com.minhthong.playlist.data.PlaylistDatabase
import com.minhthong.playlist.data.PlaylistRepositoryImpl
import com.minhthong.playlist.data.dao.PlaylistDao
import com.minhthong.playlist.domain.PlaylistBridgeImpl
import com.minhthong.playlist.domain.PlaylistRepository
import com.minhthong.playlist.domain.usecase.AddTrackToPlaylistUseCase
import com.minhthong.playlist.domain.usecase.ObserverTrackInPlaylistUseCase
import com.minhthong.playlist.presentaion.mapper.PresentationMapper
import com.minhthong.playlist_feature_api.PlaylistBridge
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePlaylistDatabase(
        @ApplicationContext context: Context
    ): PlaylistDatabase {
        return Room.databaseBuilder(
            context,
            PlaylistDatabase::class.java,
            Constant.DB_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(
        playlistDb: PlaylistDatabase
    ): PlaylistDao {
        return playlistDb.dao
    }

    @Provides
    fun provideMapper(
        context: Context
    ): PresentationMapper {
        return PresentationMapper(context = context)
    }

    @Provides
    @Singleton
    fun providePlaylistRepository(
        dao: PlaylistDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PlaylistRepository {
        return PlaylistRepositoryImpl(
            dao = dao,
            ioDispatcher = ioDispatcher
        )
    }

    @Provides
    @Singleton
    fun providePlaylistApi(
        repository: PlaylistRepository
    ): PlaylistBridge {
        return PlaylistBridgeImpl(
            addTrackToPlaylistUseCase = AddTrackToPlaylistUseCase(repository),
            observerTrackInPlaylistUseCase = ObserverTrackInPlaylistUseCase(repository)
        )
    }
}