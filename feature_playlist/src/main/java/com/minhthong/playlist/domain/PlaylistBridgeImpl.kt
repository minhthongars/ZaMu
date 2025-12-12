package com.minhthong.playlist.domain

import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.domain.usecase.AddTrackToPlaylistUseCase
import com.minhthong.playlist.domain.usecase.GetPlaylistAwareShuffleUseCase
import com.minhthong.playlist.domain.usecase.ObserverTrackInPlaylistUseCase
import com.minhthong.playlist_feature_api.PlaylistBridge
import kotlinx.coroutines.flow.Flow

class PlaylistBridgeImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val observerTrackInPlaylistUseCase: ObserverTrackInPlaylistUseCase,
    private val getPlaylistAwareShuffleUseCase: GetPlaylistAwareShuffleUseCase,
) : PlaylistBridge {

    override suspend fun addTrackToPlaylistAwareShuffle(trackEntity: TrackEntity): Result<Unit> {
        return addTrackToPlaylistUseCase.invoke(trackEntity)
    }

    override suspend fun observerTrackInPlaylist(trackId: Long): Flow<Boolean> {
        return observerTrackInPlaylistUseCase.invoke(trackId = trackId)
    }

    override fun observerPlaylist(): Flow<List<PlaylistItemEntity>> {
        return getPlaylistAwareShuffleUseCase.invoke()
    }
}