package com.minhthong.playlist.domain

import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.domain.usecase.AddTrackToPlaylistUseCase
import com.minhthong.playlist.domain.usecase.GetPlaylistAwareShuffleUseCase
import com.minhthong.playlist_feature_api.PlaylistApi
import kotlinx.coroutines.flow.Flow

class PlaylistBridgeImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val getPlaylistAwareShuffleUseCase: GetPlaylistAwareShuffleUseCase,
) : PlaylistApi {

    override suspend fun addTrackToPlaylistAwareShuffle(trackEntity: TrackEntity): Result<PlaylistItemEntity> {
        return addTrackToPlaylistUseCase.invoke(trackEntity)
    }

    override fun observerPlaylist(): Flow<List<PlaylistItemEntity>> {
        return getPlaylistAwareShuffleUseCase.invoke()
    }
}