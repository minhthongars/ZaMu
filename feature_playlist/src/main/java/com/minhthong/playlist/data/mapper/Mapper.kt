package com.minhthong.playlist.data.mapper

import android.net.Uri
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.data.model.TrackDto
import androidx.core.net.toUri

object Mapper {
    fun TrackDto.toDomain(): TrackEntity {
        return TrackEntity(
            id = id,
            sizeBytes = sizeBytes ?: 0,
            durationMs = durationMs ?: 0,
            displayName = title.orEmpty(),
            artist = artist.orEmpty(),
            album = album.orEmpty(),
            title = title.orEmpty(),
            uri = uri?.toUri() ?: Uri.EMPTY
        )
    }

    fun List<TrackDto>.toDomain(): List<TrackEntity> {
        return map { it.toDomain() }
    }

    fun TrackEntity.toData(): TrackDto {
        return TrackDto(
            id = id,
            title = title,
            album = album,
            artist = artist,
            orderIndex = null,
            durationMs = durationMs,
            sizeBytes = sizeBytes,
            uri = uri.toString()
        )
    }
}