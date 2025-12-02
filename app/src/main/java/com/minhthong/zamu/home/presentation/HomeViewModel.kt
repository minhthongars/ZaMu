package com.minhthong.zamu.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.zamu.R
import com.minhthong.zamu.core.onError
import com.minhthong.zamu.core.onSuccess
import com.minhthong.zamu.home.domain.model.TrackEntity
import com.minhthong.zamu.home.domain.model.UserEntity
import com.minhthong.zamu.home.domain.usecase.FetchUserInfoUseCase
import com.minhthong.zamu.home.domain.usecase.GetTrackFromDeviceUseCase
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterItem
import com.minhthong.zamu.home.presentation.mapper.EntityToPresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrackFromDeviceUseCase: GetTrackFromDeviceUseCase,
    private val fetchUserInfoUseCase: FetchUserInfoUseCase,
    private val mapper: EntityToPresentationMapper
) : ViewModel() {

    init {
        getDeviceTrack()
        fetchUserInfo()
    }

    private var userEntity: UserEntity? = null
    private val userInfoAdapterItemFlow = MutableStateFlow<HomeAdapterItem?>(null)

    private var deviceTrackEntities = emptyList<TrackEntity>()
    private val deviceTrackItemsFlow = MutableStateFlow<List<HomeAdapterItem>>(emptyList())

    val adapterItemsFlow: StateFlow<List<HomeAdapterItem>> = combine(
        userInfoAdapterItemFlow,
        deviceTrackItemsFlow
    ) { userInfo, deviceTracks ->
        listOfNotNull(userInfo) + deviceTracks
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = emptyList()
        )

    private fun fetchUserInfo() {
        viewModelScope.launch {
            userInfoAdapterItemFlow.update {
                HomeAdapterItem.LoadingView(
                    viewHeight = R.dimen.user_info_height
                )
            }

            fetchUserInfoUseCase.invoke()
                .onSuccess { entity ->
                    userEntity = entity
                    userInfoAdapterItemFlow.update {
                        with(mapper) { entity.toPresentation() }
                    }
                }
                .onError {
                    userInfoAdapterItemFlow.update {
                        HomeAdapterItem.ErrorView(
                            viewHeight = R.dimen.user_info_height,
                            type = HomeAdapterItem.ViewType.USER_INFO
                        )
                    }
                }
        }
    }

    private fun getDeviceTrack() {
        val titleItem = HomeAdapterItem.Title(content = "Recommend for you")
        val loadingItems = List(6) {
            HomeAdapterItem.LoadingView(
                viewHeight = R.dimen.device_track_height
            )
        }

        viewModelScope.launch {
            deviceTrackItemsFlow.update {
                listOf(titleItem) + loadingItems
            }

            getTrackFromDeviceUseCase.invoke()
                .onError {
                    val errorItem = HomeAdapterItem.ErrorView(
                        type = HomeAdapterItem.ViewType.TRACK,
                        viewHeight = R.dimen.recommend_track_height
                    )
                    deviceTrackItemsFlow.update {
                        listOf(titleItem, errorItem)
                    }
                }
                .onSuccess { trackEntities ->
                    deviceTrackEntities = trackEntities
                    val trackItems = trackEntities.map {
                        with(mapper) { it.toPresentation() }
                    }
                    deviceTrackItemsFlow.update {
                        listOf(titleItem) + trackItems
                    }
                }
        }
    }

    fun retry(viewType: Int) {
        when(viewType) {
            HomeAdapterItem.ViewType.USER_INFO -> fetchUserInfo()
            HomeAdapterItem.ViewType.TRACK -> getDeviceTrack()
        }
    }

}