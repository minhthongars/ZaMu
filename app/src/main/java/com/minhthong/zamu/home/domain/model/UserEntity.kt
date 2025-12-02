package com.minhthong.zamu.home.domain.model

data class UserEntity(
    val id: Long,
    val name: String,
    val tier: String,
    val avatarUrl: String
)