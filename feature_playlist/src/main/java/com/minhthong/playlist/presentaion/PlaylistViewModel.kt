package com.minhthong.playlist.presentaion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.R
import com.minhthong.core.toAppError
import com.minhthong.playlist.domain.usecase.GetPlaylistUseCase
import com.minhthong.playlist.presentaion.mapper.PresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    getPlaylistUseCase: GetPlaylistUseCase,
    private val mapper: PresentationMapper
): ViewModel() {

    val uiState: StateFlow<PlaylistUiState> = getPlaylistUseCase().map {
        delay(2000)
        if (it.isEmpty()) {
            PlaylistUiState.Error(messageId = R.string.empty_list_playlist)
        } else {
            val tracks = with(mapper) { it.toPresentation() }
            val successState: PlaylistUiState = PlaylistUiState.Success(tracks = tracks)
            successState
        }
    }
        .onStart {
            emit(PlaylistUiState.Loading)
        }
        .catch { e ->
            val msgId = e.toAppError().errorResId
            emit(PlaylistUiState.Error(messageId = msgId))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = PlaylistUiState.Loading
        )
}