package com.minhthong.home.domain.model

data class RemoteTrackEntity(
    val id: Long,
    val name: String,
    val performer: String,
    val avatarUrl: String,
    val mp3Url: String,
)