package com.minhthong.setting.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minhthong.playlist_feature_api.ListLongConverter

@Database(
    entities = [CutDto::class],
    version = 1
)
@TypeConverters(ListLongConverter::class)
abstract class MashupDatabase: RoomDatabase() {

    abstract val dao: MashupDao
}