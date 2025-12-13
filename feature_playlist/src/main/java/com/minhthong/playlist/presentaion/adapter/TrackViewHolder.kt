package com.minhthong.playlist.presentaion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.minhthong.playlist.databinding.ViewHolderPlaylistTrackBinding
import com.minhthong.playlist.presentaion.PlaylistUiState

class TrackViewHolder(
    private val binding: ViewHolderPlaylistTrackBinding,
    private val onItemClick: (Int, Boolean) -> Unit,
    private val onRemoveItemClick: (Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            onItemClick: (Int, Boolean) -> Unit,
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
        updatePlayingInfo(
            isPlaying = track.isPlaying,
            trackId = track.id
        )
        updateRemovingInfo(isRemoving = track.isRemoving)

        binding.ivTrackAvatar.setImageBitmap(track.avatar)
        binding.tvName.text = track.name
        binding.tvPerformer.text = track.performer

        binding.ivClose.setOnClickListener {
            onRemoveItemClick(track.id)
        }
    }

    fun updatePlayingInfo(trackId: Int, isPlaying: Boolean) {
        binding.ivPlaying.isVisible = isPlaying

        binding.root.setOnClickListener {
            onItemClick(trackId, isPlaying)
        }
    }

    fun updateRemovingInfo(isRemoving: Boolean) {
        binding.progressCircular.isVisible = isRemoving
        binding.ivClose.isVisible = isRemoving.not()
    }
}