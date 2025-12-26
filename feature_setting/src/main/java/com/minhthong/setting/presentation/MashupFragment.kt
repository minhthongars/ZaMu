package com.minhthong.setting.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.setting.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MashupFragment: Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MashupViewModel by viewModels()

    private val adapter = CutAudioAdapter(
        onItemClicked = { id ->
            viewModel.handleOnItemClicked(cutId = id)
        },
        onRemoveItemClick = { id ->
            viewModel.deleteCut(cutId = id)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        collectFlowSafely {
            viewModel.adapterItemsWithOrderFlow.collect {
                adapter.submitList(it)
            }
        }

        collectFlowSafely {
            viewModel.isLoadingFlow.collect { isLoading ->
                binding.progressCircular.isVisible = isLoading
                binding.ivShuffle.isVisible = isLoading.not()
            }
        }

        binding.checkBox.setOnClickListener {
            val isChecked = binding.checkBox.isChecked.not()
            binding.checkBox.isChecked = isChecked
            viewModel.setSelectMode(isEnable = isChecked)
        }

        binding.ivShuffle.setOnClickListener {
            viewModel.createMashup()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}