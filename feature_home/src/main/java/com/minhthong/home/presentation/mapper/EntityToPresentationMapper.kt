package com.minhthong.home.presentation.mapper

import android.content.Context
import com.minhthong.core.util.Utils
import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.core.util.Utils.toMbString
import com.minhthong.core.model.TrackEntity
import com.minhthong.home.domain.model.UserEntity
import com.minhthong.home.presentation.adapter.HomeAdapterItem
import javax.inject.Inject

class EntityToPresentationMapper @Inject constructor(
    private val context: Context
) {

    fun TrackEntity.toPresentation(): HomeAdapterItem.Track {
        val sizeString = sizeBytes.toMbString()
        val durationString = durationMs.toDurationString()

        return HomeAdapterItem.Track(
            id = id,
            name = title,
            performer = artist,
            durationString = durationString,
            sizeString = sizeString,
            avatarBitmap = Utils.getAlbumArt(context, uri)
        )
    }

    fun UserEntity.toPresentation(): HomeAdapterItem.UserInfo {
        return HomeAdapterItem.UserInfo(
            id = id,
            name = name,
            tier = "$tier member",
            avatarUrl = avatarUrl,
        )
    }
}