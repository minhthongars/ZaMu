package com.minhthong.zamu.home.presentation.mapper

import com.minhthong.zamu.home.domain.model.TrackEntity
import com.minhthong.zamu.home.domain.model.UserEntity
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterItem
import java.util.Locale
import javax.inject.Inject

class EntityToPresentationMapper @Inject constructor() {

    fun TrackEntity.toPresentation(): HomeAdapterItem.Track {
        val mb = sizeBytes / (1024.0 * 1024.0)
        val sizeString = String.format(Locale.getDefault(), "%.2f MB", mb)

        val totalSeconds = durationMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val durationString = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )

        return HomeAdapterItem.Track(
            name = title,
            performer = artist,
            durationString = durationString,
            sizeString = sizeString
        )
    }

    fun UserEntity.toPresentation(): HomeAdapterItem.UserInfo {
        return HomeAdapterItem.UserInfo(
            name = name,
            tier = "$tier member",
            avatarUrl = avatarUrl,
        )
    }
}