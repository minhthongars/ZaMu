package com.minhthong.zamu.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.minhthong.zamu.databinding.ViewHolderTrackBinding
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterItem

class TrackViewHolder(
    val binding: ViewHolderTrackBinding
): HomeViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): HomeViewHolder {
            val binding = ViewHolderTrackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TrackViewHolder(binding)
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val trackData = data as? HomeAdapterItem.Track ?: return
        binding.tvName.text = trackData.name
        binding.tvSize.text = trackData.sizeString
        binding.tvPerformer.text = trackData.performer
        binding.tvDuration.text = trackData.durationString
    }

}