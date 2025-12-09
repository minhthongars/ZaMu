package com.minhthong.playlist.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minhthong.playlist.data.Constant

@Entity(tableName = Constant.TABLE_NAME_TRACK)
data class TrackDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constant.COLUMN__ID)
    val fakeId: Int = 0,

    @ColumnInfo(name = Constant.COLUMN_ID)
    val id: Long,

    @ColumnInfo(name = Constant.COLUMN_ORDER)
    val orderIndex: Long?,

    @ColumnInfo(name = Constant.COLUMN_TITLE)
    val title: String?,

    @ColumnInfo(name = Constant.COLUMN_ARTIST)
    val artist: String?,

    @ColumnInfo(name = Constant.COLUMN_ALBUM)
    val album: String?,

    @ColumnInfo(name = Constant.COLUMN_DURATION)
    val durationMs: Long?,

    @ColumnInfo(name = Constant.COLUMN_SIZE)
    val sizeBytes: Long?,

    @ColumnInfo(name = Constant.COLUMN_PATH)
    val uri: String?
)