package com.minhthong.player.presentation.mapper

import android.content.Context
import androidx.core.content.ContextCompat
import com.minhthong.core.util.Utils
import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.core.model.PlayerEntity
import com.minhthong.player.R
import com.minhthong.player.presentation.PlayerUiModel
import com.minhthong.core.R as CR

class EntityToPresentationMapper(
    private val context: Context
) {

    fun PlayerEntity.toPresentation(): PlayerUiModel {
        val enableColor = CR.color.blue_600
        val disableColor = CR.color.grey_500

        val trackEntity = trackInfo.entity

        return PlayerUiModel(
            trackName = trackEntity.title,
            performer = trackEntity.artist,
            avatar = Utils.getAlbumArt(context, trackEntity.uri, 1024),
            album = trackEntity.album,
            duration = trackEntity.durationMs.toDurationString(),
            playIcon = if (isPlaying) {
                R.drawable.ic_player_pause
            } else {
                R.drawable.ic_player_playing
            },
            loopIconColor = ContextCompat.getColor(
                context,
                if (isLooping) enableColor else disableColor
            ),
            shuffleIconColor = ContextCompat.getColor(
                context,
                if (isShuffling) enableColor else disableColor
            ),
            sliderBarValue = trackEntity.durationMs,
        )
    }
}