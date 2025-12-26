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

    @ColumnInfo(name = DbConstant.COLUMN_PATH)
    val uri: String?,

    @ColumnInfo(name = DbConstant.COLUMN_ART)
    val avatarImage: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackDto

        if (id != other.id) return false
        if (trackId != other.trackId) return false
        if (orderIndex != other.orderIndex) return false
        if (shuffleOrderIndex != other.shuffleOrderIndex) return false
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (uri != other.uri) return false
        if (!avatarImage.contentEquals(other.avatarImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + trackId.hashCode()
        result = 31 * result + orderIndex.hashCode()
        result = 31 * result + shuffleOrderIndex.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (avatarImage?.contentHashCode() ?: 0)
        return result
    }
}