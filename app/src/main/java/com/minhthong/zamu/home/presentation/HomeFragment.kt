package com.minhthong.zamu.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.minhthong.zamu.R
import com.minhthong.zamu.databinding.FragmentHomeBinding
import com.minhthong.zamu.home.presentation.adapter.HomeAdapter
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.zamu.home.presentation.decorator.HomeRecyclerViewItemDecoration
import dagger.hilt.android.AndroidEntryPoint
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
        collectData()
    }

    private fun collectData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.adapterItemsFlow.collect {
                    homeAdapter.submitList(it)
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = homeAdapter
        binding.recyclerView.addItemDecoration(
            HomeRecyclerViewItemDecoration(
                horizontalSpace = resources.getDimensionPixelSize(R.dimen.spacing_md),
                verticalSpace = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}