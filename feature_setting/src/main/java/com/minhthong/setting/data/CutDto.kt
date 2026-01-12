package com.minhthong.setting.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cut_audio")
data class CutDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "uri")
    val uriString: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "performer")
    val performer: String,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "start_position")
    val startPosition: Long,

    @ColumnInfo(name = "end_position")
    val endPosition: Long,

    @ColumnInfo(name = "parent_track")
    val parentTrackId: List<Long>
)