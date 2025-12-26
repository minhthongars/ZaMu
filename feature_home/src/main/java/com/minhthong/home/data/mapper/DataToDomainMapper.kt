package com.minhthong.home.data.mapper

import android.content.Context
import android.net.Uri
import com.minhthong.core.util.BitmapUtils
import com.minhthong.home.domain.model.TrackEntity
import com.minhthong.home.data.model.RemoteTrackDto
import com.minhthong.home.data.model.TrackDto
import com.minhthong.home.data.model.UserDto
import com.minhthong.home.domain.model.RemoteTrackEntity
import com.minhthong.home.domain.model.UserEntity

class DataToDomainMapper(
    private val context: Context
) {

    fun TrackDto.toDomain(): TrackEntity {
        val dto = this
        return TrackEntity(
            id = dto.id ?: 0,
            title = dto.title.orEmpty(),
            artist = dto.artist.orEmpty(),
            album = dto.album.orEmpty(),
            durationMs = dto.durationMs ?: 0,
            sizeBytes = dto.sizeBytes ?: 0,
            uri = dto.uri ?: Uri.EMPTY,
            avatarBitmap = BitmapUtils.getAlbumArt(context, dto.uri)
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

    fun List<RemoteTrackDto>.toDomain(): List<RemoteTrackEntity> {
        return map { dto ->
            RemoteTrackEntity(
                id = dto.id ?: 0,
                name = dto.title.orEmpty(),
                mp3Url = dto.mp3Url.orEmpty(),
                avatarUrl = dto.avatarUrl.orEmpty(),
                performer = dto.performer.orEmpty()
            )
        }
    }
}