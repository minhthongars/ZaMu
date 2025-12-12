package com.minhthong.home.data.datasource

import com.minhthong.home.data.model.UserDto

class RemoteDataSource {

    fun fetchUserData(): UserDto {
        val dummyData = UserDto(
            id = 12,
            name = "Võ Văn Minh Thông",
            avatarUrl = "https://img.freepik.com/vector-mien-phi/hinh-minh-hoa-chang-trai-tre-mim-cuoi_1308-174669.jpg",
            tier = "Gold"
        )
        return dummyData
    }
}