package com.minhthong.zamu.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.player.PlayerManager
import com.minhthong.playlist_feature_api.PlaylistApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val playlistBridge: PlaylistApi,
    private val playerManager: PlayerManager,
): ViewModel() {

    val hasSetPlaylistFlow = playerManager.hasSetPlaylistFlow

    fun observerPlaylist() {
        playlistBridge
            .observerPlaylist()
            .map { items ->
                playerManager.setPlaylist(playlistItems = items)
            }
            .launchIn(viewModelScope)
    }
}