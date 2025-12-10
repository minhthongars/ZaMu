package com.minhthong.playlist.presentaion.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.minhthong.playlist.presentaion.PlaylistUiState

class PlaylistAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onRemoveItemClick: (Int) -> Unit
): ListAdapter<PlaylistUiState.Track, TrackViewHolder>(ItemCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrackViewHolder {
        return TrackViewHolder.create(
            parent = parent,
            onItemClick = onItemClick,
            onItemLongClick = onRemoveItemClick
        )
    }

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int
    ) {
        val track = getItem(position)
        holder.bind(track)
    }

    private class ItemCallback : DiffUtil.ItemCallback<PlaylistUiState.Track>() {
        override fun areItemsTheSame(
            oldItem: PlaylistUiState.Track,
            newItem: PlaylistUiState.Track
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PlaylistUiState.Track,
            newItem: PlaylistUiState.Track
        ): Boolean {
            return oldItem == newItem
        }
    }
}