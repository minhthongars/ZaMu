package com.minhthong.playlist.presentaion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minhthong.playlist.databinding.ViewHolderPlaylistTrackBinding
import com.minhthong.playlist.presentaion.PlaylistUiState

class TrackViewHolder(
    private val binding: ViewHolderPlaylistTrackBinding
): RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): TrackViewHolder {
            return TrackViewHolder(
                binding = ViewHolderPlaylistTrackBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    fun bind(track: PlaylistUiState.Track) {
        binding.ivTrackAvatar.setImageBitmap(track.avatar)
        binding.tvName.text = track.name
        binding.tvPerformer.text = track.performer
    }
}