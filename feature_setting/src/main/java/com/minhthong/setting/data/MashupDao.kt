package com.minhthong.setting.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MashupDao {

    @Query("SELECT * FROM cut_audio")
    fun getAllCuts(): Flow<List<CutDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCut(track: CutDto)

    @Query("DELETE FROM cut_audio WHERE id = :cutId")
    fun deleteCut(cutId: Int)

}