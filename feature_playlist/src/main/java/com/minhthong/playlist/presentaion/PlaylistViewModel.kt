package com.minhthong.playlist.presentaion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.R
import com.minhthong.core.onError
import com.minhthong.core.onSuccess
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.toAppError
import com.minhthong.playlist.domain.model.PlaylistItemEntity
import com.minhthong.playlist.domain.usecase.GetPlaylistUseCase
import com.minhthong.playlist.domain.usecase.RemoveTrackFromPlaylistUseCase
import com.minhthong.playlist.presentaion.mapper.PresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistUseCase: GetPlaylistUseCase,
    private val removeTrackUseCase: RemoveTrackFromPlaylistUseCase,
    private val playerManager: PlayerManager,
    private val mapper: PresentationMapper
): ViewModel() {

    private var playlistEntity: List<PlaylistItemEntity> = emptyList()

    private val playerInfoFlow = playerManager.playerInfoFlow

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)

    private val removingItemIdFlow = MutableStateFlow<Set<Int>>(emptySet())

    val uiState = combine(
        _uiState,
        playerInfoFlow,
        removingItemIdFlow,
    ) { state, playerInfo, removingItems ->
        val playingIndex = playerInfo?.playingTrackIndex
        val playingId = playerInfo?.trackInfo?.id

        val finalState = if (state is PlaylistUiState.Success) {
            updatePlayingInfo(
                playingId = playingId,
                playingIndex = playingIndex,
                trackUiItems = state.tracks,
                removingItems = removingItems
            )
        } else {
            state
        }

        finalState
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = PlaylistUiState.Loading
        )

    fun loadPlaylist() {
        getPlaylistUseCase.invoke().map { playlist ->
            delay(500)
            if (playlist.isEmpty()) {
                _uiState.update {
                    PlaylistUiState.Error(messageId = R.string.empty_list_playlist)
                }
            } else {
                playlistEntity = playlist
                val tracks = with(mapper) { playlist.toPresentation() }
                _uiState.update {  PlaylistUiState.Success(tracks = tracks) }
            }
        }
            .onStart {
                _uiState.update { PlaylistUiState.Loading }
            }
            .catch { e ->
                val msgId = e.toAppError().errorResId
                _uiState.update { PlaylistUiState.Error(messageId = msgId) }
            }
            .launchIn(viewModelScope)
    }

    fun playMusic(playlistItemId: Int) {
        val trackIndex = playlistEntity.indexOfFirst { it.id == playlistItemId }
        val tracks = playlistEntity.map { it.entity }

        playerManager.setPlaylist(tracks, trackIndex)
    }

    fun removePlaylistItem(playlistItemId: Int) = viewModelScope.launch {
        val items = (_uiState.value as? PlaylistUiState.Success)?.tracks.orEmpty()
        val itemIndex = items.indexOfFirst { it.id == playlistItemId }

        removingItemIdFlow.update { current ->
            current + playlistItemId
        }
        delay(300)
        removeTrackUseCase.invoke(playlistItemId)
            .onSuccess {
                playerManager.removeItem(itemIndex)
            }
            .onError {
                removingItemIdFlow.update { current ->
                    current - playlistItemId
                }
            }
    }

    private fun updatePlayingInfo(
        trackUiItems: List<PlaylistUiState.Track>,
        playingIndex: Int?,
        playingId: Long?,
        removingItems: Set<Int>
    ): PlaylistUiState {
        val tracks = trackUiItems.mapIndexed { index, track ->
            val isPlaying = index == playingIndex && track.trackId == playingId
            val isRemoving = track.id in removingItems
            track.copy(
                isPlaying = isPlaying,
                isRemoving = isRemoving
            )
        }

        return PlaylistUiState.Success(tracks = tracks)
    }
}