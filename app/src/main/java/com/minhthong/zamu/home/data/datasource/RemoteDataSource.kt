package com.minhthong.zamu.home.data.datasource

import com.minhthong.zamu.home.data.model.UserDto

class RemoteDataSource {

    var count = 0
    fun fetchUserData(): UserDto {
        if (count < 2) {
            count++
            throw NullPointerException()
        }
        val dummyData = UserDto(
            id = 12,
            name = "Võ Văn Minh Thông",
            avatarUrl = "https://img.freepik.com/vector-mien-phi/hinh-minh-hoa-chang-trai-tre-mim-cuoi_1308-174669.jpg",
            tier = "Gold"
        )
        return dummyData
    }
}