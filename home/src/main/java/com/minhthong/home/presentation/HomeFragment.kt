package com.minhthong.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.minhthong.core.R
import com.minhthong.core.Utils.collectFlowSafely
import com.minhthong.home.databinding.FragmentHomeBinding
import com.minhthong.home.presentation.adapter.HomeAdapter
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.decorator.HomeRecyclerViewItemDecoration
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    internal lateinit var navigation: Navigation

    private val homeListener = object : HomeAdapterClickListener {
        override fun onRetryClick(viewType: Int) {
            viewModel.retry(viewType)
        }

        override fun onTrackClick(trackId: Long) {
            viewModel.onTrackClick(trackId = trackId)
        }

        override fun onSaveClick(trackId: Long) {
            //later
        }
    }

    private val homeAdapter = HomeAdapter(
        listener = homeListener
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        callData()
        collectData()
    }

    private fun callData() {
        viewModel.fetchUserInfo()
        viewModel.getDeviceTrack()
    }

    private fun collectData() {
        collectFlowSafely {
            viewModel.adapterItemsFlow.collect {
                homeAdapter.submitList(it)
            }
        }

        collectFlowSafely {
            viewModel.uiEvent.collect { event ->
                when(event) {
                    is HomeUiEvent.OpenPlayer -> {
                        navigation.navigateTo(screen = Screen.PLAYER)
                    }

                    is HomeUiEvent.Toast -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        with(binding.recyclerView) {
            itemAnimator = null
            adapter = homeAdapter

            addItemDecoration(
                HomeRecyclerViewItemDecoration(
                    horizontalSpace = resources.getDimensionPixelSize(
                        R.dimen.spacing_md
                    ),
                    verticalSpace = resources.getDimensionPixelSize(
                        R.dimen.spacing_sm2
                    )
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}