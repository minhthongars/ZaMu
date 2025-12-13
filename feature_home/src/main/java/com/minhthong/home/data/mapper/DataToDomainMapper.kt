package com.minhthong.home.data.mapper

import android.net.Uri
import com.minhthong.core.model.TrackEntity
import com.minhthong.home.data.model.TrackDto
import com.minhthong.home.data.model.UserDto
import com.minhthong.home.domain.model.UserEntity

class DataToDomainMapper {

    fun TrackDto.toDomain(): TrackEntity {
        val dto = this
        return TrackEntity(
            id = dto.id ?: 0,
            title = dto.title.orEmpty(),
            artist = dto.artist.orEmpty(),
            album = dto.album.orEmpty(),
            durationMs = dto.durationMs ?: 0,
            sizeBytes = dto.sizeBytes ?: 0,
            uri = dto.uri ?: Uri.EMPTY
        )
    }

    fun UserDto.toDomain(): UserEntity {
        return UserEntity(
            name = name.orEmpty(),
            tier = tier.orEmpty(),
            avatarUrl = avatarUrl.orEmpty(),
            id = id ?: 0
        )
    }
}