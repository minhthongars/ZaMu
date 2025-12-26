package com.minhthong.playlist.domain.usecase

import android.graphics.Bitmap
import com.minhthong.core.Result
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class AddTrackToPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(
        trackId: Long,
        title: String,
        performer: String,
        uri: String,
        avatarBitmap: Bitmap?
    ): Result<PlaylistItemEntity> {
        return repository.insertTrackToPlaylist(
            trackId,
            title,
            performer,
            uri,
            avatarBitmap
        )
    }
}