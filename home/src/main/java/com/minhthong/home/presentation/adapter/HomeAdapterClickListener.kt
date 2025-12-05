package com.minhthong.home.presentation.adapter

interface HomeAdapterClickListener {
    fun onRetryClick(viewType: Int)

    fun onTrackClick(trackId: Long)

    fun onSaveClick(trackId: Long)

    fun onSaveListingClick()
}