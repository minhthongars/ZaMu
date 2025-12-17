package com.minhthong.player.presentation.mapper

import android.content.Context
import androidx.core.content.ContextCompat

import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.core.model.ControllerState
import com.minhthong.player.R
import com.minhthong.player.presentation.PlayerUiModel
import com.minhthong.core.R as CR

class EntityToPresentationMapper(
    private val context: Context
) {

    suspend fun ControllerState.toPresentation(): PlayerUiModel {
        val enableColor = CR.color.blue_600
        val disableColor = CR.color.grey_500

        return PlayerUiModel(
            trackName = playingItem.title,
            performer = playingItem.artist,
            avatar = playingItem.getAvatarBitmap(context, 1024),
            album = "Unknow album",
            duration = duration.toDurationString(),
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
                disableColor
            ),
            sliderBarValue = duration,
        )
    }
}