package com.minhthong.home.presentation

sealed class HomeUiEvent {
    object OpenPlayer: HomeUiEvent()

    object RequestAudioPermission: HomeUiEvent()

    data class Toast(val message: String): HomeUiEvent()
}