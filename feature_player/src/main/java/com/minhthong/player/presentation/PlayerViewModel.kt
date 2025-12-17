package com.minhthong.player.presentation

import androidx.lifecycle.ViewModel
import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.core.player.PlayerManager
import com.minhthong.player.presentation.mapper.EntityToPresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val mapper: EntityToPresentationMapper
) : ViewModel() {

    val uiModel = playerManager.controllerInfoFlow
        .filterNotNull()
        .map { controllerInfo ->
            with(mapper) { controllerInfo.toPresentation() }
        }

    val currentProgressString = playerManager.currentProgressMlsFlow.map {
        it.toDurationString()
    }

    private val onTouchingSeekFlow = MutableStateFlow(false)

    val currentProgressMls = playerManager.currentProgressMlsFlow.filter {
        onTouchingSeekFlow.value.not()
    }

    fun playMedia() {
        playerManager.play()
    }

    fun moveToNextMedia() {
        playerManager.moveToNext()
    }

    fun moveToPreviousMedia() {
        playerManager.moveToPrevious()
    }

    fun loopMedia() {
        playerManager.loop()
    }

    fun shuffleMedia() {

    }

    fun seek(mls: Float) {
        setIsSeeking(isTouching = false)
        playerManager.seek(mls.toLong())
    }

    fun setIsSeeking(isTouching: Boolean) {
        onTouchingSeekFlow.update { isTouching }
    }
}