package com.minhthong.core.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.minhthong.core.util.BitmapUtils

data class PlaylistItemEntity(
    val id: Int,
    val orderIndex: Long,
    val shuffleOrderIndex: Long,
    val trackId: Long,
    val title: String,
    val artist: String,
    val uri: Uri,
    val source: Source,
    val avatarUrl: String?
) {
    enum class Source {
        DEVICE,
        REMOTE;

        companion object {
            fun toSource(source: Int?): Source {
                return Source.entries.find { it.ordinal == source }!!
            }
        }
    }

    suspend fun getAvatarBitmap(
        context: Context,
        maxSize: Int = 512
    ): Bitmap? {
        return when(source) {
            Source.REMOTE -> {
                BitmapUtils.getAlbumArtFromRemote(avatarUrl)
            }
            Source.DEVICE -> {
                BitmapUtils.getAlbumArt(context, uri, maxSize)
            }
        }
    }
}