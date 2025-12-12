package com.minhthong.playlist.presentaion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.minhthong.playlist.databinding.ViewHolderPlaylistTrackBinding
import com.minhthong.playlist.presentaion.PlaylistUiState

class TrackViewHolder(
    private val binding: ViewHolderPlaylistTrackBinding,
    private val onItemClick: (Int) -> Unit,
    private val onRemoveItemClick: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            onItemClick: (Int) -> Unit,
            onItemRemoveClick: (Int) -> Unit
        ): TrackViewHolder {
            return TrackViewHolder(
                binding = ViewHolderPlaylistTrackBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onItemClick = onItemClick,
                onRemoveItemClick = onItemRemoveClick
            )
        }
    }

    fun bind(track: PlaylistUiState.Track) {
        updatePlayingInfo(isPlaying = track.isPlaying)
        updateRemovingInfo(isRemoving = track.isRemoving)

        binding.ivTrackAvatar.setImageBitmap(track.avatar)
        binding.tvName.text = track.name
        binding.tvPerformer.text = track.performer

        binding.root.setOnClickListener {
            onItemClick(track.id)
        }

        binding.ivClose.setOnClickListener {
            onRemoveItemClick(track.id)
        }
    }

    fun updatePlayingInfo(isPlaying: Boolean) {
        binding.ivPlaying.isVisible = isPlaying
        binding.ivClose.isVisible = isPlaying.not()
    }

    fun updateRemovingInfo(isRemoving: Boolean) {
        binding.progressCircular.isVisible = isRemoving
        binding.ivClose.isVisible = isRemoving.not()
    }
}