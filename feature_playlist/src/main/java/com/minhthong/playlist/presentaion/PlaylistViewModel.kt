package com.minhthong.playlist.presentaion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.R
import com.minhthong.core.model.PlaylistItemEntity
import com.minhthong.core.onError
import com.minhthong.core.onSuccess
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.toAppError
import com.minhthong.playlist.domain.usecase.GetPlaylistAwareShuffleUseCase
import com.minhthong.playlist.domain.usecase.GetShuffleEnableUseCase
import com.minhthong.playlist.domain.usecase.RemoveTrackFromPlaylistUseCase
import com.minhthong.playlist.domain.usecase.SaveShuffleEnableUseCase
import com.minhthong.playlist.domain.usecase.ShufflePlaylistUseCase
import com.minhthong.playlist.domain.usecase.UpdatePlaylistUseCase
import com.minhthong.playlist.presentaion.mapper.PresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistUseCase: GetPlaylistAwareShuffleUseCase,
    private val removeTrackUseCase: RemoveTrackFromPlaylistUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val getShuffleEnableUseCase: GetShuffleEnableUseCase,
    private val saveShuffleEnableUseCase: SaveShuffleEnableUseCase,
    private val shufflePlaylistUseCase: ShufflePlaylistUseCase,
    private val playerManager: PlayerManager,
    private val mapper: PresentationMapper
): ViewModel() {

    private var playlistItemEntities: Map<Int, PlaylistItemEntity> = emptyMap()

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)

    private val removingItemIdFlow = MutableStateFlow<Set<Int>>(emptySet())

    private val isShufflingFlow: MutableStateFlow<Boolean?> = MutableStateFlow(null)

    val uiState: StateFlow<PlaylistUiState> = combine(
        _uiState,
        removingItemIdFlow,
        playerManager.playerInfoFlow,
        isShufflingFlow.filterNotNull()
    ) { currentState, removingItems, playerInfo, isShuffling ->

        val finalState = if (currentState is PlaylistUiState.Success) {
            updatePlayingInfo(
                isShuffling = isShuffling,
                trackUiItems = currentState.tracks,
                removingItems = removingItems,
                playingItemId = playerInfo?.trackInfo?.id
            )
        } else {
            currentState
        }

        finalState
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = PlaylistUiState.Loading
        )

    fun loadData() {
        bindDatabase()
        updateIsShuffling()
    }

    private fun bindDatabase() {
        getPlaylistUseCase.invoke()
            .filter { isShufflingFlow.value != null }
            .map { playlist ->
                if (playlist.isEmpty()) {
                    showEmptyState()
                } else {
                    showSuccessState(playlistItems = playlist)
                }
            }
            .catch { throwable ->
                showErrorState(throwable = throwable)
            }
            .launchIn(viewModelScope)
    }

    private fun updateIsShuffling() = viewModelScope.launch {
        getShuffleEnableUseCase.invoke()
            .onSuccess { isShuffling ->
                isShufflingFlow.update { isShuffling }
            }
            .onError {
                isShufflingFlow.update { false }
            }
    }

    fun updateShufflePlaylist() = viewModelScope.launch {
        val isShuffling = isShufflingFlow.value ?: false

        saveShuffleEnableUseCase
            .invoke(isEnable = isShuffling.not())
            .onSuccess {
                updateIsShuffling()

                shufflePlaylistUseCase.invoke(
                    isEnableShuffle = isShuffling.not()
                )
            }
    }

    fun playMusic(playlistItemId: Int) {
        val item = playlistItemEntities[playlistItemId] ?: return
        playerManager.seekToMediaItem(playlistItemId = item.id)
    }

    fun removePlaylistItem(playlistItemId: Int) = viewModelScope.launch {

        addRemovingItemId(playlistItemId = playlistItemId)

        delay(300)
        removeTrackUseCase.invoke(playlistItemId)
            .onError { deleteRemovingItemId(playlistItemId = playlistItemId) }
    }

    fun updatePlaylistPosition(
        trackItems: List<PlaylistUiState.Track>
    ) = viewModelScope.launch {

        val playlistItems = trackItems.mapNotNull { uiItem ->
            playlistItemEntities[uiItem.id]
        }

        updatePlaylistUseCase.invoke(
            isShuffle = isShufflingFlow.value ?: false,
            tracks = playlistItems
        )
    }

    private fun updatePlayingInfo(
        trackUiItems: List<PlaylistUiState.Track>,
        removingItems: Set<Int>,
        isShuffling: Boolean,
        playingItemId: Int?
    ): PlaylistUiState {

        val tracks = trackUiItems.map { track ->
            val isRemoving = track.id in removingItems
            val isPlaying = track.id == playingItemId
            track.copy(
                isPlaying = isPlaying,
                isRemoving = isRemoving
            )
        }

        return PlaylistUiState.Success(tracks = tracks, isShuffling = isShuffling)
    }

    private fun addRemovingItemId(playlistItemId: Int) {
        removingItemIdFlow.update { current ->
            current + playlistItemId
        }
    }

    private fun deleteRemovingItemId(playlistItemId: Int) {
        removingItemIdFlow.update { current ->
            current - playlistItemId
        }
    }

    private fun showErrorState(throwable: Throwable) {
        val msgId = throwable.toAppError().errorResId
        _uiState.update { PlaylistUiState.Error(messageId = msgId) }
    }

    private fun showEmptyState() {
        _uiState.update {
            PlaylistUiState.Error(messageId = R.string.empty_list_playlist)
        }
    }

    private fun showSuccessState(playlistItems: List<PlaylistItemEntity>) {
        playlistItemEntities = playlistItems.associateBy { it.id }

        val trackUiItems = with(mapper) { playlistItems.toPresentation() }
        _uiState.update { PlaylistUiState.Success(tracks = trackUiItems, isShuffling = false) }
    }
}