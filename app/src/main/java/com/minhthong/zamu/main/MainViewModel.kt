package com.minhthong.zamu.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.player.PlayerManager
import com.minhthong.playlist_feature_api.PlaylistApi
import com.minhthong.zamu.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val playlistBridge: PlaylistApi,
    private val playerManager: PlayerManager,
): ViewModel() {

    private val currentDisplayScreen = MutableStateFlow(0)

    val controllerInfoFlow = playerManager.controllerInfoFlow

    val showMiniPlayerFlow = combine(
        controllerInfoFlow,
        currentDisplayScreen
    ) { info, screen ->
        info != null && screen != R.id.playerFragment
    }

    fun setCurrentScreen(screenId: Int) {
        currentDisplayScreen.update { screenId }
    }

    fun observerPlaylist() {
        playlistBridge
            .observerPlaylist()
            .map { items ->
                playerManager.setPlaylist(playlistItems = items)
            }
            .launchIn(viewModelScope)
    }

    fun play() {
        playerManager.playOrPause()
    }

    fun moveToNext() {
        playerManager.moveToNext()
    }

    fun moveToPrev() {
        playerManager.moveToPrevious()
    }
}