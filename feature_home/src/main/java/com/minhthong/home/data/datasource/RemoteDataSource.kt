package com.minhthong.home.data.datasource

import com.minhthong.home.data.model.RemoteTrackDto
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

    fun fetchPremiumTrack(): List<RemoteTrackDto> {
        return listOf(
            RemoteTrackDto(
                id = 1,
                title = "Hãy Trao Cho Anh",
                performer = "Sơn Tùng M-TP",
                mp3Url = "https://a01.nct.vn/NhacCuaTui985/HayTraoChoAnh-SonTungMTPSnoopDogg-6010660_hq.mp3",
                avatarUrl = "https://image-cdn.nct.vn/song/2019/07/03/7/5/b/e/1562137543919_300.jpg"
            ),
            RemoteTrackDto(
                id = 12,
                title = "Chúng ta hông thuộc về nhau",
                performer = "Minh Thông Võ",
                mp3Url = "https://stream.nct.vn/NhacCuaTui925/ChungTaKhongThuocVeNhau-SonTungMTP-4528181_hq.mp3",
                avatarUrl = "https://media.licdn.com/dms/image/v2/C5603AQG9onurlCskFQ/profile-displayphoto-shrink_200_200/profile-displayphoto-shrink_200_200/0/1633177468994?e=1767225600&v=beta&t=W4c79pm9KYHZQ0Mmdx60GRFgT9-oy-EztCf6rJOGMNk"
            ),
            RemoteTrackDto(
                id = 11,
                title = "Lạc trôi",
                performer = "Jack97",
                mp3Url = "https://stream.nct.vn/NhacCuaTui934/LacTroi-SonTungMTP-4725907_hq.mp3",
                avatarUrl = "https://image-cdn.nct.vn/song/2024/03/15/4/c/b/d/1710498563935_300.jpg"
            ),
            RemoteTrackDto(
                id = 10,
                title = "Chúng ta của hiện tại",
                performer = "Sơn Tùng M-TP",
                mp3Url = "https://a01.nct.vn/NhacCuaTui2053/ChungTaCuaHienTai-SonTungMTP-6892340_hq.mp3",
                avatarUrl = "https://image-cdn.nct.vn/song/2024/03/15/4/c/b/d/1710493302970_300.jpg"
            ),
        )
    }
}