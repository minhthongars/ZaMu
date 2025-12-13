package com.minhthong.playlist.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minhthong.playlist.data.DbConstant

@Entity(tableName = DbConstant.TABLE_NAME_TRACK)
data class TrackDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DbConstant.COLUMN_ID)
    val id: Int = 0,

    @ColumnInfo(name = DbConstant.COLUMN_TRACK_ID)
    val trackId: Long,

    @ColumnInfo(name = DbConstant.COLUMN_ORDER)
    val orderIndex: Long,

    @ColumnInfo(name = DbConstant.COLUMN_SHUFFLE_ORDER)
    val shuffleOrderIndex: Long,

    @ColumnInfo(name = DbConstant.COLUMN_TITLE)
    val title: String?,

    @ColumnInfo(name = DbConstant.COLUMN_ARTIST)
    val artist: String?,

    @ColumnInfo(name = DbConstant.COLUMN_ALBUM)
    val album: String?,

    @ColumnInfo(name = DbConstant.COLUMN_DURATION)
    val durationMs: Long?,

    @ColumnInfo(name = DbConstant.COLUMN_SIZE)
    val sizeBytes: Long?,

    @ColumnInfo(name = DbConstant.COLUMN_PATH)
    val uri: String?,

    @ColumnInfo(name = DbConstant.COLUMN_IS_PLAYING)
    val isPlaying: Boolean
)