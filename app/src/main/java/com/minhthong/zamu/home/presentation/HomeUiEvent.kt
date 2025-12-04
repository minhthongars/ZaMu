package com.minhthong.zamu.home.presentation

import com.minhthong.zamu.home.domain.model.TrackEntity

sealed class HomeUiEvent {
    object OpenPlayer: HomeUiEvent()

    data class Toast(val message: String): HomeUiEvent()
}