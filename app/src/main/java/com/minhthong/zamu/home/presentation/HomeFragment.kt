package com.minhthong.zamu.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.minhthong.zamu.R
import com.minhthong.zamu.core.Utils.collectFlowSafely
import com.minhthong.zamu.databinding.FragmentHomeBinding
import com.minhthong.zamu.home.presentation.adapter.HomeAdapter
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.zamu.home.presentation.decorator.HomeRecyclerViewItemDecoration
import com.minhthong.zamu.main.MainFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

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

        viewModel.uiEvent.onEach { event ->
            when(event) {
                is HomeUiEvent.OpenPlayer -> {
                    (parentFragment?.parentFragment as MainFragment).safeNavigate(R.id.playerFragment)
                }

                is HomeUiEvent.Toast -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setUpRecyclerView() {
        with(binding.recyclerView) {
            itemAnimator = null
            adapter = homeAdapter
            addItemDecoration(
                HomeRecyclerViewItemDecoration(
                    horizontalSpace = resources.getDimensionPixelSize(R.dimen.spacing_md),
                    verticalSpace = resources.getDimensionPixelSize(R.dimen.spacing_sm2)
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}