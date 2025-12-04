package com.minhthong.zamu.home.presentation.adapter

interface HomeAdapterClickListener {
    fun onRetryClick(viewType: Int)

    fun onTrackClick(trackId: Long)

    fun onSaveClick(trackId: Long)
}