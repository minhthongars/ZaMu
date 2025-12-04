package com.minhthong.player.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.slider.Slider
import com.minhthong.core.Utils.collectFlowSafely
import com.minhthong.core.Utils.toDurationString
import com.minhthong.player.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment: Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModels()

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

        viewModel.loadData()
        setupViews()
        setUpCollectors()
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
            viewModel.shuffleMedia()
        }

        setUpSliderBar()
    }

    private fun setUpSliderBar() {
        binding.sliderSeek.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    viewModel.setIsSeeking(isTouching = true)
                }
                override fun onStopTrackingTouch(slider: Slider) {
                    viewModel.seek(slider.value)
                }
            }
        )

        binding.sliderSeek.setLabelFormatter { value ->
            val mls = value.toLong()
            mls.toDurationString()
        }
    }

    private fun setUpCollectors() {
        collectFlowSafely {
            viewModel.uiModel.collect { info ->
                binding.tvTrackTitle.text = info.trackName
                binding.tvAlbum.text = info.album
                binding.tvArtist.text = info.performer
                binding.tvTotalTime.text = info.duration
                binding.btnPlayPause.setImageResource(info.playIcon)

                ImageViewCompat.setImageTintList(
                    binding.btnRepeat,
                    ColorStateList.valueOf(info.loopIconColor)
                )
                ImageViewCompat.setImageTintList(
                    binding.btnShuffle,
                    ColorStateList.valueOf(info.shuffleIconColor)
                )
                binding.ivCoverArt.setImageBitmap(info.avatar)
                binding.sliderSeek.valueTo = info.sliderBarValue.toFloat()
            }
        }

        collectFlowSafely {
            viewModel.currentProgressString.collect {
                binding.tvCurrentTime.text = it
            }
        }

        collectFlowSafely {
            viewModel.currentProgressMls.collect {
                binding.sliderSeek.value = it.toFloat()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
