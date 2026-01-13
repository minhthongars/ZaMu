package com.minhthong.playlist.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minhthong.playlist.data.DbConstant

@Entity(tableName = DbConstant.TABLE_NAME_AVATAR)
class AvatarDto(
    @PrimaryKey
    @ColumnInfo(name = DbConstant.COLUMN_TRACK_ID)
    val trackId: Long,

    @ColumnInfo(name = DbConstant.COLUMN_ART)
    val byteArray: ByteArray?
)