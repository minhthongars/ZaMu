package com.minhthong.playlist.domain

import com.minhthong.core.Result
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.domain.usecase.AddTrackToPlaylistUseCase
import com.minhthong.playlist.domain.usecase.ObserverTrackInPlaylistUseCase
import com.minhthong.playlist_feature_api.PlaylistBridge
import kotlinx.coroutines.flow.Flow

class PlaylistBridgeImpl(
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val observerTrackInPlaylistUseCase: ObserverTrackInPlaylistUseCase
) : PlaylistBridge {

    override suspend fun addTrackToPlaylist(trackEntity: TrackEntity): Result<Unit> {
        return addTrackToPlaylistUseCase.invoke(trackEntity)
    }

    override suspend fun observerTrackInPlaylist(trackId: Long): Flow<Boolean> {
        return observerTrackInPlaylistUseCase.invoke(trackId = trackId)
    }
}