package com.minhthong.zamu.home.data.datasource

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import com.minhthong.zamu.home.data.model.TrackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceDataSource(
    private val contentResolver: ContentResolver,
) {

    fun getTracksFromDevice(): List<TrackDto> {
        val tracks = ArrayList<TrackDto>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.SIZE} > 0"
        val selectionArgs: Array<String>? = null
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: ""
                val displayName = cursor.getString(displayNameCol) ?: ""
                val artist = cursor.getString(artistCol) ?: ""
                val album = cursor.getString(albumCol) ?: ""
                val duration = cursor.getLong(durationCol)
                val size = cursor.getLong(sizeCol)

                val contentUri = ContentUris.withAppendedId(queryUri, id)

                tracks.add(
                    TrackDto(
                        id = id,
                        title = title,
                        displayName = displayName,
                        artist = artist,
                        album = album,
                        durationMs = duration,
                        sizeBytes = size,
                        uri = contentUri
                    )
                )
            }
        }

        return tracks
    }
}
