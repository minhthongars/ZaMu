package com.minhthong.playlist_feature_api

import androidx.room.TypeConverter

class ListLongConverter {
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        return value?.split(",")?.filter { it.isNotEmpty() }?.map { it.toLong() }
    }
}