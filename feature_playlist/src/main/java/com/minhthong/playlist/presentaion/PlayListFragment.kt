package com.minhthong.playlist.presentaion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.SimpleItemAnimator
import com.minhthong.core.R
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import com.minhthong.playlist.databinding.FragmentPlayListBinding
import com.minhthong.playlist.presentaion.adapter.PlaylistAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayListFragment: Fragment() {

    private var _binding: FragmentPlayListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()

    @Inject
    internal lateinit var navigation: Navigation

    private val onItemClickListener: (Int) -> Unit = { id ->
        viewModel.playMusic(playlistItemId = id)
    }

    private val onRemoveItemClick: (Int) -> Unit = { id ->
        viewModel.removePlaylistItem(playlistItemId = id)
    }

    private val adapter = PlaylistAdapter(
        onItemClick = onItemClickListener,
        onRemoveItemClick = onRemoveItemClick
    )

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
        viewModel.loadPlaylist()
    }

    private fun setupViews() {
        binding.llLoading.isVisible = true
        binding.flError.isVisible = false

        val itemAnimator = binding.recyclerView.itemAnimator
        if (itemAnimator is SimpleItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
            itemAnimator.moveDuration = 220
            itemAnimator.changeDuration = 220
        }
        binding.recyclerView.isVisible = false

        binding.recyclerView.adapter = adapter
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
                        successContent(tracks = state.tracks)
                    }
                }
            }
        }
    }

    private fun loadingContent() {
        binding.llLoading.isVisible = true
        binding.flError.isVisible = false
        binding.recyclerView.isVisible = false

        startLoadingAnimation()
    }

    private fun errorContent(messageId: Int) {
        binding.llLoading.isVisible = false
        binding.flError.isVisible = true
        binding.recyclerView.isVisible = false
        clearLoadingAnimation()

        binding.tvErrorMessage.text = getText(messageId)
        binding.flError.setOnClickListener {
            navigation.navigateTo(Screen.HOME)
        }
    }

    private fun successContent(tracks: List<PlaylistUiState.Track>) {
        binding.llLoading.isVisible = false
        binding.flError.isVisible = false
        binding.recyclerView.isVisible = true
        clearLoadingAnimation()

        adapter.submitList(tracks)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}