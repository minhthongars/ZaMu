package com.minhthong.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.minhthong.home.databinding.ViewHolderTrackBinding
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class TrackViewHolder(
    val binding: ViewHolderTrackBinding,
    val listener: HomeAdapterClickListener,
): HomeViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            listener: HomeAdapterClickListener
        ): HomeViewHolder {
            val binding = ViewHolderTrackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TrackViewHolder(binding, listener)
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val trackData = data as? HomeAdapterItem.Track ?: return

        with(binding) {
            tvName.text = trackData.name
            tvSize.text = trackData.sizeString
            tvPerformer.text = trackData.performer
            tvDuration.text = trackData.durationString
            ivTrackAvatar.setImageBitmap(trackData.avatarBitmap)

            root.setOnClickListener {
                listener.onTrackClick(trackData.id)
            }

            binding.ivAdd.setOnClickListener {
                listener.onAddToPlaylistClick(trackData.id)
            }
        }
    }

}