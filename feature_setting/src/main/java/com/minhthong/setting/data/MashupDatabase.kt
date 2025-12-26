package com.minhthong.setting.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CutDto::class],
    version = 1
)
abstract class MashupDatabase: RoomDatabase() {

    abstract val dao: MashupDao
}