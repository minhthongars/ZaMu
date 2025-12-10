package com.minhthong.playlist.data.mapper

import android.net.Uri
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.data.model.TrackDto
import androidx.core.net.toUri
import com.minhthong.playlist.domain.model.PlaylistItemEntity

object Mapper {

    private fun TrackDto.toDomain(): PlaylistItemEntity {
        val entity = TrackEntity(
            id = id,
            sizeBytes = sizeBytes ?: 0,
            durationMs = durationMs ?: 0,
            displayName = title.orEmpty(),
            artist = artist.orEmpty(),
            album = album.orEmpty(),
            title = title.orEmpty(),
            uri = uri?.toUri() ?: Uri.EMPTY
        )
        return PlaylistItemEntity(
            id = fakeId,
            entity = entity
        )
    }

    fun List<TrackDto>.toDomain(): List<PlaylistItemEntity> {
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