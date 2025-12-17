package com.minhthong.home.presentation.adapter

interface HomeAdapterClickListener {
    fun onRetryClick(viewType: Int)

    fun onTrackClick(trackId: Long)

    fun onRemoteTrackClick(trackId: Long)

    fun onAddToPlaylistClick(trackId: Long)

    fun onAddRemoteTrackToPlaylistClick(trackId: Long)

    fun onSaveListingClick()
}