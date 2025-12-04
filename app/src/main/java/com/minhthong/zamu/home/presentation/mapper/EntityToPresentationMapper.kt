package com.minhthong.zamu.home.presentation.mapper

import android.content.Context
import com.minhthong.zamu.core.Utils
import com.minhthong.zamu.core.Utils.toDurationString
import com.minhthong.zamu.core.Utils.toMbString
import com.minhthong.zamu.home.domain.model.TrackEntity
import com.minhthong.zamu.home.domain.model.UserEntity
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterItem
import java.util.Locale
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