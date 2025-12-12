package com.minhthong.playlist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minhthong.playlist.data.dao.PlaylistDao
import com.minhthong.playlist.data.model.TrackDto

@Database(
    entities = [TrackDto::class],
    version = DbConstant.DB_VERSION
)
abstract class PlaylistDatabase: RoomDatabase() {

    abstract val dao: PlaylistDao
}