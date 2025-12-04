package com.minhthong.home.presentation

sealed class HomeUiEvent {
    object OpenPlayer: HomeUiEvent()

    data class Toast(val message: String): HomeUiEvent()
}