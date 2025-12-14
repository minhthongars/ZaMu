package com.minhthong.playlist.presentaion

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.minhthong.core.R
import com.minhthong.core.util.NotificationPermissionHelper
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import com.minhthong.playlist.databinding.FragmentPlayListBinding
import com.minhthong.playlist.presentaion.adapter.ItemTouchHelperCallback
import com.minhthong.playlist.presentaion.adapter.PlaylistAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class PlayListFragment: Fragment() {

    private var _binding: FragmentPlayListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()

    @Inject
    internal lateinit var navigation: Navigation

    private val notificationPermissionHelper = NotificationPermissionHelper(
        wFragment = WeakReference(this)
    )

    private val onItemClickListener: (Int, Boolean) -> Unit = { id, isPlaying ->
        if (isPlaying) {
            navigation.navigateTo(Screen.PLAYER)
        } else {
            requestNotificationPermissionAndPlayMusic(playlistItemId = id)
        }
    }

    private val onRemoveItemClick: (Int) -> Unit = { id ->
        viewModel.removePlaylistItem(playlistItemId = id)
    }

    private val adapter = PlaylistAdapter(
        onItemClick = onItemClickListener,
        onRemoveItemClick = onRemoveItemClick
    )

    private val onMoveCallback = { fromPosition: Int, toPosition: Int ->
        adapter.moveItem(
            fromPosition = fromPosition,
            toPosition = toPosition
        )
    }

    private val onDropCallback: () -> Unit = {
        viewModel.updatePlaylistPosition(
            trackItems = adapter.currentList
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setUpCollector()
        viewModel.updateIsShuffling()
    }

    private fun setupViews() {
        val itemAnimator = binding.recyclerView.itemAnimator
        if (itemAnimator is SimpleItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
            itemAnimator.moveDuration = 220
            itemAnimator.changeDuration = 220
        }

        binding.recyclerView.adapter = adapter

        val callback = ItemTouchHelperCallback(onMoveCallback, onDropCallback)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recyclerView)

        binding.ivShuffle.setOnClickListener {
            viewModel.updateShufflePlaylist()
        }
    }

    private fun setUpCollector() {
        collectFlowSafely {
            viewModel.uiState.collect { state ->
                when(state) {
                    is PlaylistUiState.Loading -> {
                        loadingContent()
                    }

                    is PlaylistUiState.Error -> {
                       errorContent(messageId = state.messageId)
                    }

                    is PlaylistUiState.Success -> {
                        successContent(tracks = state.tracks, isShuffling = state.isShuffling)
                    }
                }
            }
        }
    }

    private fun loadingContent() {
        binding.llLoading.isVisible = true
        binding.flError.isVisible = false
        binding.llSuccess.isVisible = false

        startLoadingAnimation()
    }

    private fun errorContent(messageId: Int) {
        binding.llLoading.isVisible = false
        binding.flError.isVisible = true
        binding.llSuccess.isVisible = false
        clearLoadingAnimation()

        binding.tvErrorMessage.text = getText(messageId)
        binding.flError.setOnClickListener {
            navigation.navigateTo(Screen.HOME)
        }
    }

    private fun successContent(tracks: List<PlaylistUiState.Track>, isShuffling: Boolean) {
        binding.llLoading.isVisible = false
        binding.flError.isVisible = false
        binding.llSuccess.isVisible = true
        clearLoadingAnimation()

        val color = ContextCompat.getColor(
            requireContext(),
            if (isShuffling) R.color.blue_600
            else R.color.grey_700
        )
        ImageViewCompat.setImageTintList(
            binding.ivShuffle,
            ColorStateList.valueOf(color)
        )

       saveStateSubmitList(tracks)
    }

    private fun saveStateSubmitList(tracks: List<PlaylistUiState.Track>) {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val scrollPosition = layoutManager.findFirstVisibleItemPosition()
        val view = layoutManager.findViewByPosition(scrollPosition)
        val scrollOffset = view?.top ?: 0

        adapter.submitList(tracks) {
            layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset)
        }
    }

    private fun startLoadingAnimation() {
        clearLoadingAnimation()

        val shimmerAnimation = AnimationUtils.loadAnimation(
            context,
            R.anim.shimmer_translate
        )

        for (i in 0..<binding.llLoading.childCount) {
            val frameLayout = binding.llLoading.getChildAt(i) as? ViewGroup ?: return
            frameLayout.getChildAt(1).startAnimation(shimmerAnimation)
        }
    }

    private fun clearLoadingAnimation() {
        for (i in 0..<binding.llLoading.childCount) {
            val frameLayout = binding.llLoading.getChildAt(i) as? ViewGroup ?: return
            frameLayout.getChildAt(1).clearAnimation()
        }
    }

    private fun requestNotificationPermissionAndPlayMusic(playlistItemId: Int) {
        notificationPermissionHelper.requestPermissionAndStartService(
            onGranted = {
                viewModel.playMusic(playlistItemId = playlistItemId)
            },
            onDenied = {
                viewModel.playMusic(playlistItemId = playlistItemId)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationPermissionHelper.cleanup()
        _binding = null
    }
}