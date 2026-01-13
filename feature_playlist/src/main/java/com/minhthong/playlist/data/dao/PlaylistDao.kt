package com.minhthong.playlist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.minhthong.playlist.data.DbConstant
import com.minhthong.playlist.data.model.AvatarDto
import com.minhthong.playlist.data.model.TrackDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM ${DbConstant.TABLE_NAME_TRACK}")
    fun getPlaylist(): Flow<List<TrackDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackDto): Long

    @Query("SELECT * FROM ${DbConstant.TABLE_NAME_TRACK} WHERE ${DbConstant.COLUMN_ID} = :itemId")
    suspend fun getTrackById(itemId: Long): TrackDto?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAvatar(track: AvatarDto)

    @Query("SELECT * FROM ${DbConstant.TABLE_NAME_AVATAR}")
    suspend fun getAllAvatar(): List<AvatarDto>

    @Query("DELETE FROM ${DbConstant.TABLE_NAME_TRACK} WHERE ${DbConstant.COLUMN_ID} = :playlistItemId")
    suspend fun deleteTrackById(playlistItemId: Long)

    @Query("SELECT COALESCE(MAX(${DbConstant.COLUMN_ORDER}), -1) + 1 FROM ${DbConstant.TABLE_NAME_TRACK}")
    suspend fun getNextOrderIndex(): Long

    @Query("SELECT COALESCE(MAX(${DbConstant.COLUMN_SHUFFLE_ORDER}), -1) + 1 FROM ${DbConstant.TABLE_NAME_TRACK}")
    suspend fun shuffleOrderIndex(): Long

    @Upsert
    suspend fun upsertTracks(tracks: List<TrackDto>)
}