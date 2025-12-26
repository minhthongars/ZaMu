package com.minhthong.setting.presentation

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.common.onError
import com.minhthong.core.common.onSuccess
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.util.BitmapUtils
import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.feature_mashup_api.entity.CutEntity
import com.minhthong.feature_mashup_api.repository.MashupRepository
import com.minhthong.playlist_feature_api.PlaylistApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MashupViewModel @Inject constructor(
    private val repository: MashupRepository,
    private val playlistApi: PlaylistApi,
    private val playerManager: PlayerManager
): ViewModel() {

    private var cutEntities: List<CutEntity> = emptyList()

    private val selectModeEnabledFlow = MutableStateFlow(false)

    private val selectedItemMapFlow = MutableStateFlow(mapOf<Int, Int>())

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow = _isLoadingFlow.asStateFlow()

    private val rawAdapterItemsFlow = repository.getAllCuts().map { entities ->
        cutEntities = entities
        entities.toPresentation()
    }

    val adapterItemsWithOrderFlow = combine(
        rawAdapterItemsFlow,
        selectedItemMapFlow,
        selectModeEnabledFlow
    ) { adapterItems, selectedItems, selectedEnable ->
        if (selectedEnable) {
            adapterItems.map { item ->
                item.copy(
                    order = selectedItems[item.id]
                )
            }
        } else {
            adapterItems
        }
    }

    fun handleOnItemClicked(cutId: Int) = viewModelScope.launch {
        if (selectModeEnabledFlow.value) {
            addToSelectedItems(cutId)
        } else {
            addToPlaylist(cutId)
        }
    }

    private fun addToSelectedItems(cutId: Int) {
        val selectedItemMap = selectedItemMapFlow.value.toMutableMap()
        if (selectedItemMap.contains(cutId)) {
            selectedItemMap.remove(cutId)

            selectedItemMap.entries
                .sortedBy { it.value }
                .forEachIndexed { index, entry ->
                    selectedItemMap[entry.key] = index + 1
                }
        } else {
            val index = selectedItemMap.size + 1
            selectedItemMap[cutId] = index
        }

        selectedItemMapFlow.update { selectedItemMap }
    }

    private fun List<CutEntity>.toPresentation(): List<CutAdapterItem> {
        return this.map { entity ->
            val startPos = entity.startPosition
            val endPos = entity.endPosition

            val startPosString = startPos.toDurationString()
            val endPosString = endPos.toDurationString()
            val totalCutDurationString = (endPos - startPos).toDurationString()
            val totalDurationString = entity.duration.toDurationString()

            val cutInfo = "$startPosString - $endPosString  ||  ${totalCutDurationString}/${totalDurationString}"

            CutAdapterItem(
                id = entity.id,
                name = entity.name,
                avatar = entity.avatar,
                cutInfo = cutInfo,
                order = null
            )
        }
    }

    private suspend fun addToPlaylist(cutId: Int) {
        _isLoadingFlow.update { true }

        cutEntities.find { it.id == cutId }?.let { entity ->

            val startPosString = entity.startPosition.toDurationString()
            val endPosString = entity.endPosition.toDurationString()

            val result = playlistApi.addTrackToPlaylistAwareShuffle(
                trackId = entity.id.toLong(),
                title = "[$startPosString - $endPosString] ${entity.name}",
                performer = entity.performer,
                uri = entity.uri.toString(),
                avatarBitmap = entity.avatar
            )

            result.onSuccess { playlistItem ->
                playerManager.seekToLastMediaItem(playlistItem)
                _isLoadingFlow.update { false }
            }.onError {
                _isLoadingFlow.update { false }
            }
        }
    }

    fun setSelectMode(isEnable: Boolean) {
        selectModeEnabledFlow.update { isEnable }
    }

    fun createMashup() = viewModelScope.launch {
        _isLoadingFlow.update { true }

        val selectedItemMap = selectedItemMapFlow.value
        val cutList = selectedItemMap.entries
            .sortedBy { it.value }
            .mapNotNull { (key, _) ->
                cutEntities.find { it.id == key }
            }

        val uriList = cutList.map { it.uri }

        val filePath = playerManager.createMashup(uriList)

        val name = cutList.map { it.name }.distinct().joinToString(" - ")
        val performer = cutList.map { it.performer }.distinct().joinToString(" - ")
        val duration = cutList.sumOf { it.endPosition - it.startPosition }
        val bitmaps = cutList.mapNotNull { it.avatar }.distinct()

        repository.insertCut(
            uriString = filePath.toUri().toString(),
            name = name,
            performer = performer,
            duration = duration,
            startPosition = 0,
            endPosition = duration,
            avatarBitmap = BitmapUtils.mergeBitmapsGrid(bitmaps)
        )

        _isLoadingFlow.update { false }
    }

    fun deleteCut(cutId: Int) = viewModelScope.launch {
        repository.removeCut(cutId)
    }
}