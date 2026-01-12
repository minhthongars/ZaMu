package com.minhthong.playlist.domain

import android.graphics.Bitmap
import com.minhthong.core.common.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.playlist.domain.usecase.AddMashupToPlaylistUseCase
import com.minhthong.playlist.domain.usecase.AddTrackToPlaylistUseCase
import com.minhthong.playlist.domain.usecase.GetAllAvatarUseCase
import com.minhthong.playlist.domain.usecase.GetPlaylistAwareShuffleUseCase
import com.minhthong.playlist_feature_api.PlaylistApi
import kotlinx.coroutines.flow.Flow

class PlaylistBridgeImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val getPlaylistAwareShuffleUseCase: GetPlaylistAwareShuffleUseCase,
    private val getAllAvatarUseCase: GetAllAvatarUseCase,
    private val addMashupToPlaylistUseCase: AddMashupToPlaylistUseCase
) : PlaylistApi {

    override suspend fun addTrackToPlaylistAwareShuffle(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        avatarBitmap: Bitmap?
    ): Result<PlaylistItemEntity> {
        return addTrackToPlaylistUseCase.invoke(
            trackId,
            title,
            performer,
            uri,
            avatarBitmap
        )
    }

    override suspend fun addMashupToPlaylistAwareShuffle(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        parentTrackId: List<Long>
    ): Result<PlaylistItemEntity> {
        return addMashupToPlaylistUseCase.invoke(
            trackId,
            title,
            performer,
            uri,
            parentTrackId
        )
    }

    override fun observerPlaylist(): Flow<List<PlaylistItemEntity>> {
        return getPlaylistAwareShuffleUseCase.invoke()
    }

    override suspend fun getAllAvatar(): Map<Long, ByteArray?> {
        return getAllAvatarUseCase.invoke()
    }
}