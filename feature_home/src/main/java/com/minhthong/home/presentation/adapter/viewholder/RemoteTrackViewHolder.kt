package com.minhthong.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.minhthong.core.util.Utils
import com.minhthong.core.util.Utils.loadImage
import com.minhthong.home.databinding.ViewHolderRemoteTrackBinding
import com.minhthong.home.databinding.ViewHolderTrackBinding
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class RemoteTrackViewHolder(
    val binding: ViewHolderRemoteTrackBinding,
    val listener: HomeAdapterClickListener,
): HomeViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            listener: HomeAdapterClickListener
        ): HomeViewHolder {
            val binding = ViewHolderRemoteTrackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return RemoteTrackViewHolder(binding, listener)
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val trackData = data as? HomeAdapterItem.RemoteTrack ?: return

        with(binding) {
            tvName.text = trackData.name
            tvPerformer.text = trackData.performer
            ivTrackAvatar.loadImage(trackData.avatar)

            root.setOnClickListener {
                listener.onRemoteTrackClick(trackData.id)
            }

            binding.ivAdd.setOnClickListener {
                //listener.onAddToPlaylistClick(trackData.id)
            }
        }

        bindLoading(isLoading = trackData.isLoading)
    }

    fun bindLoading(isLoading: Boolean) {
        binding.progressCircular.isVisible = isLoading
        binding.ivAdd.isVisible = isLoading.not()
    }

}