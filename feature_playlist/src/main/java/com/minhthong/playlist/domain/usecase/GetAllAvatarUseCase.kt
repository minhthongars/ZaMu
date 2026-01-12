package com.minhthong.playlist.domain.usecase

import com.minhthong.playlist.domain.PlaylistRepository
import javax.inject.Inject

class GetAllAvatarUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {

    suspend operator fun invoke(): Map<Long, ByteArray?> {
        return repository.getAllAvatar()
    }
}