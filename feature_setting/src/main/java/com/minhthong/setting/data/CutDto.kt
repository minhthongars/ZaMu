package com.minhthong.setting.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cut_audio")
data class CutDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

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

    @ColumnInfo(name = "avatar")
    val avatarImage: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CutDto

        if (id != other.id) return false
        if (duration != other.duration) return false
        if (startPosition != other.startPosition) return false
        if (endPosition != other.endPosition) return false
        if (uriString != other.uriString) return false
        if (name != other.name) return false
        if (performer != other.performer) return false
        if (!avatarImage.contentEquals(other.avatarImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + duration.hashCode()
        result = 31 * result + startPosition.hashCode()
        result = 31 * result + endPosition.hashCode()
        result = 31 * result + uriString.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + performer.hashCode()
        result = 31 * result + avatarImage.contentHashCode()
        return result
    }
}