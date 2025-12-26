package com.minhthong.player.presentation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.feature_mashup_api.repository.MashupRepository
import com.minhthong.player.presentation.mapper.EntityToPresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.first
import kotlin.collections.last

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val mapper: EntityToPresentationMapper,
    private val mashupRepository: MashupRepository
) : ViewModel() {

    private val onTouchingSeekFlow = MutableStateFlow(false)

    private val onCuttingAudioFlow = MutableStateFlow(false)

    private val _playRangeFlow = MutableStateFlow(LongRange(0, 0))
    private val playRange get() = _playRangeFlow.value
    val playRangeFlow = _playRangeFlow.asStateFlow()

    val currentBufferMls = playerManager.currentBufferMlsFlow

    val uiModel = combine(
        playerManager.controllerInfoFlow.filterNotNull(),
        onCuttingAudioFlow
    ) { controllerInfo, isCutting ->
            with(mapper) {
                controllerInfo.toPresentation(
                    isAudioCutting = isCutting
                )
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )
        .filterNotNull()

    val currentProgressString = playerManager.currentProgressMlsFlow.map { mls ->
        mls.toDurationString()
    }

    val currentProgressMls = playerManager.currentProgressMlsFlow
        .filter {
            onTouchingSeekFlow.value.not()
        }
        .map { positionMls ->
            handlePlayInRange(positionMls)

            positionMls
        }

    private fun handlePlayInRange(positionMls: Long) {
        val playTo = playRange.last
        val playFrom = playRange.first

        if (playTo == 0L && playFrom == 0L) {
            return
        }
        val newPlayTo = if (playFrom > 0) {
            playTo - 155
        } else {
            playTo
        }

        when {
            positionMls >= newPlayTo -> {
                playerManager.seek(positionMs = playFrom)
            }
            positionMls < playFrom -> {
                playerManager.seek(positionMs = playFrom)
            }
        }
    }

    private fun resetPlayRange() {
        val newRange = LongRange(0, 0)
        _playRangeFlow.update { newRange }
    }

    fun setPlayRange(values: List<Float>) {
        val newRange = LongRange(
            values.first().toLong(),
            values.last().toLong()
        )
        _playRangeFlow.update { newRange }
    }

    fun playMedia() {
        playerManager.playOrPause()
    }

    fun moveToNextMedia() {
        resetPlayRange()
        playerManager.moveToNext()
    }

    fun moveToPreviousMedia() {
        resetPlayRange()
        playerManager.moveToPrevious()
    }

    fun loopMedia() {
        playerManager.loopOrNot()
    }

    fun seek(mls: Float) {
        setIsSeeking(isTouching = false)
        playerManager.seek(mls.toLong())
    }

    fun setIsSeeking(isTouching: Boolean) {
        onTouchingSeekFlow.update { isTouching }
    }

    fun cutAudio() = viewModelScope.launch {
        val controllerInfo = playerManager.controllerInfoFlow.value ?: return@launch

        onCuttingAudioFlow.update { true }

        val playingItemEntity = controllerInfo.playingItem

        val startPosition = playRange.first
        val endPosition = playRange.last

        val outputPath = playerManager.cutAudio(
            startMls = startPosition,
            endMls = endPosition
        )

        mashupRepository.insertCut(
            name = playingItemEntity.title,
            startPosition = startPosition,
            endPosition = endPosition,
            duration = controllerInfo.duration,
            uriString = outputPath,
            performer = playingItemEntity.artist,
            avatarBitmap = playingItemEntity.avatarImage
        )

        onCuttingAudioFlow.update { false }
    }
}