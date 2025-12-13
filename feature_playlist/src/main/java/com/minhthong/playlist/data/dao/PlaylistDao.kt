package com.minhthong.playlist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.minhthong.playlist.data.DbConstant
import com.minhthong.playlist.data.model.TrackDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM ${DbConstant.TABLE_NAME_TRACK}")
    fun getPlaylist(): Flow<List<TrackDto>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ${DbConstant.TABLE_NAME_TRACK}
            WHERE ${DbConstant.COLUMN_TRACK_ID} = :trackId
        )
        """
    )
    fun observeTrackInPlaylist(trackId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackDto): Long

    @Query("SELECT * FROM ${DbConstant.TABLE_NAME_TRACK} WHERE ${DbConstant.COLUMN_ORDER} = :order")
    suspend fun getTrackByOrder(order: Long): TrackDto?

    @Query("DELETE FROM ${DbConstant.TABLE_NAME_TRACK} WHERE ${DbConstant.COLUMN_ID} = :playlistItemId")
    suspend fun deleteTrackById(playlistItemId: Int): Int

    @Query("SELECT COALESCE(MAX(${DbConstant.COLUMN_ORDER}), -1) + 1 FROM ${DbConstant.TABLE_NAME_TRACK}")
    suspend fun getNextOrderIndex(): Long

    @Query("SELECT COALESCE(MAX(${DbConstant.COLUMN_SHUFFLE_ORDER}), -1) + 1 FROM ${DbConstant.TABLE_NAME_TRACK}")
    suspend fun shuffleOrderIndex(): Long

    @Upsert
    suspend fun upsertTracks(tracks: List<TrackDto>)
}