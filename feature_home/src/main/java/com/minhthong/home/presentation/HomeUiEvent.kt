package com.minhthong.home.presentation

sealed class HomeUiEvent {
    object OpenPlayer: HomeUiEvent()

    object RequestAudioPermission: HomeUiEvent()

    object RequestPostNotificationPermission: HomeUiEvent()

    data class Toast(val messageId: Int): HomeUiEvent()
}