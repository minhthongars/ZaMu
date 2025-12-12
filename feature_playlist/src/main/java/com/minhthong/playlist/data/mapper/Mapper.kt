package com.minhthong.playlist.data.mapper

import android.net.Uri
import com.minhthong.core.model.TrackEntity
import com.minhthong.playlist.data.model.TrackDto
import androidx.core.net.toUri
import com.minhthong.core.model.PlaylistItemEntity

object Mapper {

    private fun TrackDto.toDomain(): PlaylistItemEntity {
        val entity = TrackEntity(
            id = trackId,
            sizeBytes = sizeBytes ?: 0,
            durationMs = durationMs ?: 0,
            displayName = title.orEmpty(),
            artist = artist.orEmpty(),
            album = album.orEmpty(),
            title = title.orEmpty(),
            uri = uri?.toUri() ?: Uri.EMPTY
        )
        return PlaylistItemEntity(
            id = id,
            entity = entity,
            orderIndex = orderIndex ?: 0,
            shuffleOrderIndex = shuffleOrderIndex ?: 0,
            isPlaying = false
        )
    }

    fun List<TrackDto>.toDomain(): List<PlaylistItemEntity> {
        return map { it.toDomain() }
    }

    fun TrackEntity.toData(): TrackDto {
        return TrackDto(
            trackId = id,
            title = title,
            album = album,
            artist = artist,
            orderIndex = null,
            durationMs = durationMs,
            sizeBytes = sizeBytes,
            uri = uri.toString(),
            isPlaying = false,
            shuffleOrderIndex = 0
        )
    }

    fun PlaylistItemEntity.toData(): TrackDto {
        return TrackDto(
            trackId = entity.id,
            title = entity.title,
            album = entity.album,
            artist = entity.artist,
            orderIndex = orderIndex,
            durationMs = entity.durationMs,
            sizeBytes = entity.sizeBytes,
            uri = entity.uri.toString(),
            id = id,
            isPlaying = isPlaying,
            shuffleOrderIndex = shuffleOrderIndex
        )
    }
}