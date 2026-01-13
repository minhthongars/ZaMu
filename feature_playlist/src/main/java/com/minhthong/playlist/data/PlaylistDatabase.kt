package com.minhthong.playlist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minhthong.playlist.data.dao.PlaylistDao
import com.minhthong.playlist.data.model.AvatarDto
import com.minhthong.playlist.data.model.TrackDto
import com.minhthong.playlist_feature_api.ListLongConverter

@Database(
    entities = [TrackDto::class, AvatarDto::class],
    version = DbConstant.DB_VERSION
)
@TypeConverters(ListLongConverter::class)
abstract class PlaylistDatabase: RoomDatabase() {

    abstract val dao: PlaylistDao
}