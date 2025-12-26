package com.minhthong.home.presentation.mapper

import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.core.util.Utils.toMbString
import com.minhthong.home.domain.model.RemoteTrackEntity
import com.minhthong.home.domain.model.TrackEntity
import com.minhthong.home.domain.model.UserEntity
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class EntityToPresentationMapper {

    fun TrackEntity.toPresentation(): HomeAdapterItem.Track {
        val sizeString = sizeBytes.toMbString()
        val durationString = durationMs.toDurationString()

        return HomeAdapterItem.Track(
            id = id,
            name = title,
            performer = artist,
            durationString = durationString,
            sizeString = sizeString,
            avatarBitmap = avatarBitmap,
            isLoading = false
        )
    }

    fun RemoteTrackEntity.toPresentation(): HomeAdapterItem.RemoteTrack {
        return HomeAdapterItem.RemoteTrack(
            id = id,
            name = name,
            performer = performer,
            avatar = avatarUrl,
            isLoading = false
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