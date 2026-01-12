package com.minhthong.playlist.data.mapper

import android.net.Uri
import androidx.core.net.toUri
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.util.Utils.toBitmap
import com.minhthong.core.util.Utils.toByteArray
import com.minhthong.playlist.data.model.TrackDto

object Mapper {

    fun TrackDto.toDomain(
        avatarImage: ByteArray?
    ): PlaylistItemEntity {

        return PlaylistItemEntity(
            id = id,
            orderIndex = orderIndex,
            shuffleOrderIndex = shuffleOrderIndex,
            trackId = trackId,
            artist = artist.orEmpty(),
            title = title.orEmpty(),
            uri = uri?.toUri() ?: Uri.EMPTY,
            avatarImage = listOf(avatarImage.toBitmap())
        )
    }

    fun TrackDto.toDomain(
        avatarImages: List<ByteArray?>
    ): PlaylistItemEntity {

        return PlaylistItemEntity(
            id = id,
            orderIndex = orderIndex,
            shuffleOrderIndex = shuffleOrderIndex,
            trackId = trackId,
            artist = artist.orEmpty(),
            title = title.orEmpty(),
            uri = uri?.toUri() ?: Uri.EMPTY,
            avatarImage = avatarImages.map { it.toBitmap() }
        )
    }

    fun PlaylistItemEntity.toData(): TrackDto {
        return TrackDto(
            id = id,
            trackId = trackId,
            title = title,
            artist = artist,
            orderIndex = orderIndex,
            uri = uri.toString(),
            shuffleOrderIndex = shuffleOrderIndex,
            parentTrackId = emptyList()
        )
    }
}