package com.minhthong.zamu.home.data.mapper

import android.net.Uri
import com.minhthong.zamu.home.data.model.TrackDto
import com.minhthong.zamu.home.data.model.UserDto
import com.minhthong.zamu.home.domain.model.TrackEntity
import com.minhthong.zamu.home.domain.model.UserEntity

class DataToDomainMapper {

    fun TrackDto.toDomain(): TrackEntity {
        val dto = this
        return TrackEntity(
            id = dto.id ?: 0,
            title = dto.title.orEmpty(),
            displayName = dto.displayName.orEmpty(),
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