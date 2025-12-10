package com.minhthong.playlist.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.minhthong.playlist.data.Constant
import com.minhthong.playlist.data.model.TrackDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM ${Constant.TABLE_NAME_TRACK}")
    fun getPlaylist(): Flow<List<TrackDto>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ${Constant.TABLE_NAME_TRACK}
            WHERE ${Constant.COLUMN_ID} = :trackId
        )
        """
    )
    fun observeTrackInPlaylist(trackId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackDto)

    @Update
    suspend fun updateTrack(track: TrackDto)

    @Query("SELECT * FROM tracks WHERE ${Constant.COLUMN_ORDER} = :order LIMIT 1")
    suspend fun getTrackByOrder(order: Int): TrackDto?

    @Query("UPDATE tracks SET ${Constant.COLUMN_ORDER} = :newOrder WHERE id = :trackId")
    suspend fun updateOrder(trackId: Long?, newOrder: Int?)

    @Query("DELETE FROM ${Constant.TABLE_NAME_TRACK} WHERE ${Constant.COLUMN__ID} = :playlistItemId")
    suspend fun deleteTrackById(playlistItemId: Int): Int

    @Query("SELECT COALESCE(MAX(${Constant.COLUMN_ORDER}), -1) + 1 FROM ${Constant.TABLE_NAME_TRACK}")
    suspend fun getNextOrderIndex(): Long
}