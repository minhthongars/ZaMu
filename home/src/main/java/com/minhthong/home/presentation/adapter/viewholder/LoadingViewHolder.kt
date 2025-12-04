package com.minhthong.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.minhthong.home.R
import com.minhthong.home.databinding.ViewHolderLoadingBinding
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class LoadingViewHolder(
    private val binding: ViewHolderLoadingBinding
): HomeViewHolder(binding.root) {

    private val shimmerAnimation = AnimationUtils.loadAnimation(
        binding.root.context,
        R.anim.shimmer_translate
    )

    companion object {
        fun create(
            parent: ViewGroup,
        ): LoadingViewHolder {
            return LoadingViewHolder(
                binding = ViewHolderLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ),
            )
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val loadingData = data as? HomeAdapterItem.LoadingView ?: return

        val layoutParams = itemView.layoutParams
        layoutParams.height = itemView.resources.getDimensionPixelSize(loadingData.viewHeight)
        itemView.layoutParams = layoutParams

        startShimmer()
    }

    fun startShimmer() {
        binding.shimmerOverlay.clearAnimation()
        binding.shimmerOverlay.startAnimation(shimmerAnimation)
    }

    fun stopShimmer() {
        binding.shimmerOverlay.clearAnimation()
    }
}
