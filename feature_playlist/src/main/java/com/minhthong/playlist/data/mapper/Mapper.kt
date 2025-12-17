package com.minhthong.playlist.data.mapper

import android.net.Uri
import com.minhthong.playlist.data.model.TrackDto
import androidx.core.net.toUri
import com.minhthong.core.model.PlaylistItemEntity

object Mapper {

    fun TrackDto.toDomain(): PlaylistItemEntity {
        return PlaylistItemEntity(
            id = id,
            orderIndex = orderIndex,
            shuffleOrderIndex = shuffleOrderIndex,
            trackId = trackId,
            artist = artist.orEmpty(),
            title = title.orEmpty(),
            uri = uri?.toUri() ?: Uri.EMPTY
        )
    }

    fun List<TrackDto>.toDomain(): List<PlaylistItemEntity> {
        return map { it.toDomain() }
    }

    fun PlaylistItemEntity.toData(): TrackDto {
        return TrackDto(
            id = id,
            trackId = trackId,
            title = title,
            artist = artist,
            orderIndex = orderIndex,
            uri = uri.toString(),
            shuffleOrderIndex = shuffleOrderIndex
        )
    }
}