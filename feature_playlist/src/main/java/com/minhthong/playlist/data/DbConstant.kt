package com.minhthong.playlist.data

object DbConstant {
    const val DB_NAME = "playlist"
    const val DB_VERSION = 1
    const val TABLE_NAME_TRACK = "tracks"
    const val COLUMN_ID = "id"
    const val COLUMN_TRACK_ID = "Track_id"
    const val COLUMN_ORDER = "order_index"
    const val COLUMN_SHUFFLE_ORDER = "shuffle_order_index"
    const val COLUMN_TITLE = "title"
    const val COLUMN_ARTIST = "artist"
    const val COLUMN_ALBUM = "album"
    const val COLUMN_DURATION = "duration"
    const val COLUMN_SIZE = "size"
    const val COLUMN_PATH = "path"
    const val COLUMN_IS_PLAYING = "is_playing"
    const val GAP_ORDER = 500L
}