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
import com.minhthong.playlist_feature_api.PlaylistApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.minhthong.core.R as CR

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrackFromDeviceUseCase: GetTrackFromDeviceUseCase,
    private val fetchUserInfoUseCase: FetchUserInfoUseCase,
    private val playlistApi: PlaylistApi,
    private val playerManager: PlayerManager,
    private val mapper: EntityToPresentationMapper
) : ViewModel() {

    private var playTrackId: Long = 0

    private var userEntity: UserEntity? = null
    private val userInfoAdapterItemFlow = MutableStateFlow<HomeAdapterItem?>(null)

    private var deviceTrackEntities = emptyList<TrackEntity>()
    private val deviceTrackUiItemsFlow = MutableStateFlow(value = getDeviceTrackLoadingItems())
    private val addingToPlaylistTrackIdsFlow = MutableStateFlow<Set<Long>>(emptySet())

    private val deviceTrackUiItemsWithLoadingFlow = combine(
        deviceTrackUiItemsFlow,
        addingToPlaylistTrackIdsFlow
    ) { trackAdapterItems, addingItems ->
        val newUiItems = if (addingItems.isNotEmpty()) {
            updateTrackLoadingInfo(
                trackAdapterItems = trackAdapterItems,
                addingItems = addingItems
            )
        } else {
            trackAdapterItems
        }

        newUiItems
    }

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val uiEvent = _uiEvent.asSharedFlow()

    val homeUiItemsFlow: StateFlow<List<HomeAdapterItem>> = combine(
        userInfoAdapterItemFlow,
        deviceTrackUiItemsWithLoadingFlow
    ) { userInfo, deviceTracks ->
        listOfNotNull(userInfo) + deviceTracks
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = emptyList()
        )

    fun fetchUserInfo() = viewModelScope.launch {
        showUserInfoLoadingItem()

        fetchUserInfoUseCase.invoke()
            .onSuccess { entity ->
                showUserInfoSuccessItem(entity = entity)
            }
            .onError { messageId ->
                showUserInfoErrorItem(messageId = messageId)
            }
    }

    fun getDeviceTrack() = viewModelScope.launch {
        showTrackLoadingItems()

        delay(800)
        getTrackFromDeviceUseCase.invoke()
            .onError { messageId ->
                showTrackErrorItems(messageId)
            }
            .onSuccess { trackEntities ->
                if (trackEntities.isEmpty()) {
                    showTrackErrorItems(messageId = CR.string.empty_list_track)
                } else {
                    showTrackSuccessItems(trackEntities)
                }
            }
    }

    private fun updateTrackLoadingInfo(
        trackAdapterItems: List<HomeAdapterItem>,
        addingItems: Set<Long>
    ): List<HomeAdapterItem> {
        return trackAdapterItems.map { item ->
            if (item is HomeAdapterItem.Track) {
                val isLoading = item.id in addingItems
                item.copy(isLoading = isLoading)
            } else {
                item
            }
        }
    }

    private fun showUserInfoErrorItem(messageId: Int) {
        val userInfoSectionHeightDimenRes = R.dimen.user_info_section_height
        val errorItem = HomeAdapterItem.ErrorView(
            viewHeight = userInfoSectionHeightDimenRes,
            message = messageId,
            type = HomeAdapterItem.ViewType.USER_INFO
        )
        userInfoAdapterItemFlow.update { errorItem }
    }

    private fun showUserInfoSuccessItem(entity: UserEntity) {
        userEntity = entity

        val adapterItems = with(mapper) { entity.toPresentation() }
        userInfoAdapterItemFlow.update { adapterItems }
    }

    private fun showUserInfoLoadingItem() {
        val userInfoSectionHeightDimenRes = R.dimen.user_info_section_height
        val loadingItem = HomeAdapterItem.LoadingView(
            viewHeight = userInfoSectionHeightDimenRes
        )
        userInfoAdapterItemFlow.update { loadingItem }
    }

    private fun showTrackLoadingItems() {
        deviceTrackUiItemsFlow.update {
            getDeviceTrackLoadingItems()
        }
    }

    private fun showTrackErrorItems(messageId: Int) {
        val errorItems = getDeviceTrackErrorItems(
            messageId = messageId
        )
        deviceTrackUiItemsFlow.update { errorItems }
    }

    private fun showTrackSuccessItems(trackEntities: List<TrackEntity>) {
        deviceTrackEntities = trackEntities

        val titleItem = getDeviceTrackTitleItem()
        val trackItems = trackEntities.map {
            with(mapper) { it.toPresentation() }
        }

        deviceTrackUiItemsFlow.update {
            listOf(titleItem) + trackItems
        }
    }

    fun showPermissionDenyError() {
        val errorItems = getDeviceTrackErrorItems(
            messageId = CR.string.grant_permission_msg
        )
        deviceTrackUiItemsFlow.update { errorItems }
    }

    fun retry(viewType: Int) {
        when(viewType) {
            HomeAdapterItem.ViewType.USER_INFO -> {
                fetchUserInfo()
            }

            HomeAdapterItem.ViewType.TRACK -> {
                _uiEvent.tryEmit(HomeUiEvent.RequestAudioPermission)
            }
        }
    }

    fun handlePlayMusic(trackId: Long) {
        playTrackId = trackId
        _uiEvent.tryEmit(HomeUiEvent.RequestPostNotificationPermission)
    }

    fun playMusic() = viewModelScope.launch {
        val clickedTrack = deviceTrackEntities.find { it.id == playTrackId }

        if (clickedTrack == null) {
            showToast(CR.string.common_error_retry_msg)
            return@launch
        }

        playlistApi.addTrackToPlaylistAwareShuffle(trackEntity = clickedTrack)
            .onSuccess { playlistItem ->
                playerManager.seekToLastMediaItem(playlistItem)
                _uiEvent.tryEmit(HomeUiEvent.OpenPlayer)
            }
            .onError { messageId ->
                showToast(messageId)
            }
    }

    fun addToPlaylist(trackId: Long) = viewModelScope.launch {
        val trackEntity = deviceTrackEntities.find { it.id == trackId }

        if (trackEntity == null) {
            showToast(msgId = CR.string.common_error_mgs)
            return@launch
        }

        addingToPlaylistTrackIdsFlow.update { currentItems ->
            currentItems + trackId
        }

        delay(150)
        playlistApi.addTrackToPlaylistAwareShuffle(trackEntity)
            .onSuccess {
                addingToPlaylistTrackIdsFlow.update { currentItems ->
                    currentItems - trackId
                }
            }
            .onError { msgId ->
                showToast(msgId = msgId)
                addingToPlaylistTrackIdsFlow.update { currentItems ->
                    currentItems - trackId
                }
            }
    }

    fun showToast(msgId: Int) {
        _uiEvent.tryEmit(HomeUiEvent.Toast(messageId = msgId))
    }

    private fun getDeviceTrackTitleItem(): HomeAdapterItem {
        return HomeAdapterItem.Title(content = R.string.recommend_for_you)
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
        messageId: Int
    ) : List<HomeAdapterItem> {
        val titleItem = getDeviceTrackTitleItem()
        val errorItem = HomeAdapterItem.ErrorView(
            type = HomeAdapterItem.ViewType.TRACK,
            message = messageId,
            viewHeight = R.dimen.recommend_track_section_height
        )

        return listOf(titleItem) + errorItem
    }
}