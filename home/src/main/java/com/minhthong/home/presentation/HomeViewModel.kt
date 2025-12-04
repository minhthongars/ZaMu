package com.minhthong.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.onError
import com.minhthong.core.onSuccess
import com.minhthong.core.player.PlayerManager
import com.minhthong.home.R
import com.minhthong.home.domain.model.UserEntity
import com.minhthong.home.domain.usecase.FetchUserInfoUseCase
import com.minhthong.home.domain.usecase.GetTrackFromDeviceUseCase
import com.minhthong.home.presentation.adapter.HomeAdapterItem
import com.minhthong.home.presentation.mapper.EntityToPresentationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrackFromDeviceUseCase: GetTrackFromDeviceUseCase,
    private val fetchUserInfoUseCase: FetchUserInfoUseCase,
    private val playerManager: PlayerManager,
    private val mapper: EntityToPresentationMapper
) : ViewModel() {

    private var userEntity: UserEntity? = null
    private val userInfoAdapterItemFlow = MutableStateFlow<HomeAdapterItem?>(null)

    private var deviceTrackEntities = emptyList<TrackEntity>()
    private val deviceTrackAdapterItemsFlow = MutableStateFlow<List<HomeAdapterItem>>(emptyList())

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val adapterItemsFlow: StateFlow<List<HomeAdapterItem>> = combine(
        userInfoAdapterItemFlow,
        deviceTrackAdapterItemsFlow
    ) { userInfo, deviceTracks ->
        listOfNotNull(userInfo) + deviceTracks
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = emptyList()
        )

    fun fetchUserInfo() = viewModelScope.launch {
        val userInfoSectionHeightDimenRes = R.dimen.user_info_section_height
        val loadingItem = HomeAdapterItem.LoadingView(
            viewHeight = userInfoSectionHeightDimenRes
        )
        userInfoAdapterItemFlow.update { loadingItem }

        fetchUserInfoUseCase.invoke()
            .onSuccess { entity ->
                userEntity = entity

                val adapterItems = with(mapper) { entity.toPresentation() }
                userInfoAdapterItemFlow.update { adapterItems }
            }
            .onError { message ->
                val errorItem = HomeAdapterItem.ErrorView(
                    viewHeight = userInfoSectionHeightDimenRes,
                    message = message,
                    type = HomeAdapterItem.ViewType.USER_INFO
                )
                userInfoAdapterItemFlow.update { errorItem }
            }
    }

    fun getDeviceTrack() = viewModelScope.launch {
        val titleItem = HomeAdapterItem.Title(content = "Recommend for you")
        val loadingItems = List(6) {
            HomeAdapterItem.LoadingView(
                viewHeight = R.dimen.device_track_item_height
            )
        }

        deviceTrackAdapterItemsFlow.update {
            listOf(titleItem) + loadingItems
        }

        getTrackFromDeviceUseCase.invoke()
            .onError { message ->
                val errorItem = HomeAdapterItem.ErrorView(
                    type = HomeAdapterItem.ViewType.TRACK,
                    message = message,
                    viewHeight = R.dimen.recommend_track_section_height
                )
                deviceTrackAdapterItemsFlow.update {
                    listOf(titleItem, errorItem)
                }
            }
            .onSuccess { trackEntities ->
                if (trackEntities.isEmpty()) {
                    val errorItem = HomeAdapterItem.ErrorView(
                        type = HomeAdapterItem.ViewType.TRACK,
                        message = "Bạn chả có nhạc gì cả, đi tải đi",
                        viewHeight = R.dimen.message_section_height
                    )
                    deviceTrackAdapterItemsFlow.update {
                        listOf(titleItem, errorItem)
                    }
                    return@onSuccess
                }

                deviceTrackEntities = trackEntities
                val trackItems = trackEntities.map {
                    with(mapper) { it.toPresentation() }
                }
                deviceTrackAdapterItemsFlow.update {
                    listOf(titleItem) + trackItems
                }
            }
    }

    fun retry(viewType: Int) {
        when(viewType) {
            HomeAdapterItem.ViewType.USER_INFO -> fetchUserInfo()
            HomeAdapterItem.ViewType.TRACK -> getDeviceTrack()
        }
    }

    fun onTrackClick(trackId: Long) {
        val clickedIndex = deviceTrackEntities.indexOfFirst { it.id == trackId }

        playerManager.setPlaylist(deviceTrackEntities, clickedIndex)

        _uiEvent.trySend(HomeUiEvent.OpenPlayer)
    }
}