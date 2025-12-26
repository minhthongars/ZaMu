package com.minhthong.player.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.core.util.Utils.toDurationString
import com.minhthong.player.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment: Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModels()

    private var animationJob: Job? = null

    private val onSliderTouchListener = object : Slider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: Slider) {
            viewModel.setIsSeeking(isTouching = true)
        }

        override fun onStopTrackingTouch(slider: Slider) {
            val seekToValue = slider.value
            viewModel.seek(seekToValue)
            binding.tvCurrentTime.text = seekToValue.toDurationString()
        }
    }

    private val onRangeSliderTouchListener = object : RangeSlider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: RangeSlider) = Unit

        override fun onStopTrackingTouch(slider: RangeSlider) {
            viewModel.setPlayRange(values = slider.values)
        }

    }

    private val sliderLabelFormatter = LabelFormatter { mls ->
        mls.toDurationString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setUpCollectors()

        binding.sliderRange.values = listOf(10F, 80F)
    }

    private fun setupViews() {
        binding.btnPlayPause.setOnClickListener {
            viewModel.playMedia()
        }

        binding.btnNext.setOnClickListener {
            viewModel.moveToNextMedia()
        }

        binding.btnPrevious.setOnClickListener {
            viewModel.moveToPreviousMedia()
        }

        binding.btnRepeat.setOnClickListener {
            viewModel.loopMedia()
        }

        binding.btnShuffle.setOnClickListener {
            viewModel.cutAudio()
        }

        setUpSliderBar()
    }

    private fun setUpSliderBar() {
        with(binding) {
            sliderSeek.setLabelFormatter(sliderLabelFormatter)
            sliderSeek.addOnSliderTouchListener(onSliderTouchListener)

            sliderRange.addOnSliderTouchListener(onRangeSliderTouchListener)
            sliderRange.setLabelFormatter(sliderLabelFormatter)
        }
    }

    private fun setUpCollectors() {
        collectFlowSafely {
            viewModel.uiModel.collect { info ->
               binData(info)
            }
        }

        collectFlowSafely {
            viewModel.currentProgressString.collect {
                binding.tvCurrentTime.text = it
            }
        }

        collectFlowSafely {
            viewModel.currentProgressMls.collect { mls ->
                binding.sliderSeek.value = mls.toFloat()
            }
        }

        collectFlowSafely {
            viewModel.currentBufferMls.collect { mls ->
                binding.sliderSecondary.value = mls.toFloat()
            }
        }

        collectFlowSafely {
            viewModel.playRangeFlow.collect { range ->
                binding.sliderRange.values = listOf(
                    range.first.toFloat(),
                    range.last.toFloat()
                )
            }
        }
    }

    private fun startAnimation(isStart: Boolean) {
        if (isStart.not()) {
            animationJob?.cancel()
            return
        }
        animationJob?.cancel()
        animationJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                binding.cvCoverArt.rotation += 1.2F
                delay(16)
            }
        }
    }

    private fun binData(info: PlayerUiModel) {
        if (info.sliderBarValue > 0) {
            val valueTo = info.sliderBarValue.toFloat()
            binding.sliderSeek.valueTo = valueTo
            binding.sliderSecondary.valueTo = valueTo

            if (binding.sliderRange.valueTo != valueTo) {
                binding.sliderRange.valueTo = valueTo
                viewModel.setPlayRange(values = listOf(0F, valueTo))
            }
        }

        binding.tvTrackTitle.text = info.trackName
        binding.tvArtist.text = info.performer
        binding.tvTotalTime.text = info.duration

        binding.btnPlayPause.setImageResource(info.playIcon)

        startAnimation(isStart = info.startAnimation)

        ImageViewCompat.setImageTintList(
            binding.btnRepeat,
            ColorStateList.valueOf(info.loopIconColor)
        )
        ImageViewCompat.setImageTintList(
            binding.btnShuffle,
            ColorStateList.valueOf(info.shuffleIconColor)
        )

        binding.ivCoverArt.setImageBitmap(info.avatar)

        binding.btnShuffle.isInvisible = info.isAudioCutting
        binding.progressCircular.isVisible = info.isAudioCutting

        binding.tvTrackTitle.isSelected = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
