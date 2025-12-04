package com.minhthong.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.Utils.toDurationString
import com.minhthong.core.player.PlayerManager
import com.minhthong.player.presentation.mapper.EntityToPresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val mapper: EntityToPresentationMapper
) : ViewModel() {

    private val _uiModel = MutableStateFlow(PlayerUiModel())
    val uiModel = _uiModel.asStateFlow()

    private val onTouchingSeekFlow = MutableStateFlow(false)

    val currentProgressString = playerManager.currentProgressMls().map {
        it.toDurationString()
    }

    val currentProgressMls = playerManager.currentProgressMls().filter {
        onTouchingSeekFlow.value.not()
    }

    fun loadData() = viewModelScope.launch {
        playerManager.playerInfo()
            .filterNotNull()
            .collect { info ->
                _uiModel.update {
                    with(mapper) { info.toPresentation() }
                }
            }
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
        playerManager.shuffle()
    }

    fun seek(mls: Float) {
        setIsSeeking(isTouching = false)
        playerManager.seek(mls.toLong())
    }

    fun setIsSeeking(isTouching: Boolean) {
        onTouchingSeekFlow.update { isTouching }
    }
}