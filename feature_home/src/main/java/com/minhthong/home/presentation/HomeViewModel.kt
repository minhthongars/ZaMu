package com.minhthong.home.presentation

import android.util.Log
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
    private val deviceTrackAdapterItemsFlow = MutableStateFlow(value = getDeviceTrackLoadingItems())

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
        val titleItem = getDeviceTrackTitleItem()

        deviceTrackAdapterItemsFlow.update {
            getDeviceTrackLoadingItems()
        }

        getTrackFromDeviceUseCase.invoke()
            .onError { message ->
                val errorItems = getDeviceTrackErrorItems(
                    message = message
                )
                deviceTrackAdapterItemsFlow.update { errorItems }
            }
            .onSuccess { trackEntities ->
                if (trackEntities.isEmpty()) {
                    val errorItems = getDeviceTrackErrorItems(
                        message = "Bạn chưa có nhạc, hãy đi tải đi nhé!"
                    )
                    deviceTrackAdapterItemsFlow.update { errorItems }
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

    fun showPermissionDenyError() {
        val errorItems = getDeviceTrackErrorItems(
            message = "Cấp quyền đi mới scan nhạc được á!"
        )
        deviceTrackAdapterItemsFlow.update { errorItems }
    }

    fun retry(viewType: Int) {
        when(viewType) {
            HomeAdapterItem.ViewType.USER_INFO -> {
                fetchUserInfo()
            }

            HomeAdapterItem.ViewType.TRACK -> {
                _uiEvent.trySend(HomeUiEvent.RequestAudioPermission)
            }
        }
    }

    fun onTrackClick(trackId: Long) {
        val clickedIndex = deviceTrackEntities.indexOfFirst { it.id == trackId }

        playerManager.setPlaylist(deviceTrackEntities, clickedIndex)

        _uiEvent.trySend(HomeUiEvent.OpenPlayer)
    }

    private fun getDeviceTrackTitleItem(): HomeAdapterItem {
        return HomeAdapterItem.Title(content = "Recommend for you")
    }

    private fun getDeviceTrackLoadingItems(): List<HomeAdapterItem> {
        val titleItem = getDeviceTrackTitleItem()
        val loadingItems = List(6) {
            HomeAdapterItem.LoadingView(
                viewHeight = R.dimen.device_track_item_height
            )
        }
        return listOf(titleItem) + loadingItems
    }

    private fun getDeviceTrackErrorItems(
        message: String
    ) : List<HomeAdapterItem> {
        val titleItem = getDeviceTrackTitleItem()
        val errorItem = HomeAdapterItem.ErrorView(
            type = HomeAdapterItem.ViewType.TRACK,
            message = message,
            viewHeight = R.dimen.recommend_track_section_height
        )

        return listOf(titleItem) + errorItem
    }
}