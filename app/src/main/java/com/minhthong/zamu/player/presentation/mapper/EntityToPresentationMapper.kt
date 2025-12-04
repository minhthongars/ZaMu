package com.minhthong.zamu.player.presentation.mapper

import android.content.Context
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import com.minhthong.zamu.R
import com.minhthong.zamu.core.Utils
import com.minhthong.zamu.core.Utils.toDurationString
import com.minhthong.zamu.core.player.PlayerEntity
import com.minhthong.zamu.player.presentation.PlayerUiModel

class EntityToPresentationMapper(
    private val context: Context
) {

    fun PlayerEntity.toPresentation(): PlayerUiModel {
        val enableColor = R.color.blue_600
        val disableColor = R.color.grey_500
        return PlayerUiModel(
            trackName = trackInfo.displayName,
            performer = trackInfo.artist,
            avatar = Utils.getAlbumArt(context, trackInfo.uri, 1024),
            album = trackInfo.album,
            duration = trackInfo.durationMs.toDurationString(),
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
            sliderBarValue = trackInfo.durationMs
        )
    }
}