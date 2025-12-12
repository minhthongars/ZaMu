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
            onItemRemoveClick = onRemoveItemClick
        )
    }

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int
    ) {
        val track = getItem(position)
        holder.bind(track)
    }

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int,
        payloads: List<Any?>
    ) {
        val payload = payloads.firstOrNull()
        val item = getItem(position)

        when(payload) {
            PAY_LOAD_PLAYING -> holder.updatePlayingInfo(isPlaying = item.isPlaying)

            PAY_LOAD_REMOVING -> holder.updateRemovingInfo(isRemoving = item.isRemoving)

            else -> super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun moveItem(
        fromPosition: Int,
        toPosition: Int,
    ) {
        val list = currentList.toMutableList()
        val item = list.removeAt(fromPosition)
        list.add(toPosition, item)

        submitList(list)
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

        override fun getChangePayload(
            oldItem: PlaylistUiState.Track,
            newItem: PlaylistUiState.Track
        ): Any? {
            return if (oldItem.isPlaying != newItem.isPlaying) {
                PAY_LOAD_PLAYING
            } else if (oldItem.isRemoving != newItem.isRemoving) {
                PAY_LOAD_REMOVING
            } else {
                null
            }
        }
    }

    companion object {
        private const val PAY_LOAD_PLAYING = 10
        private const val PAY_LOAD_REMOVING = 20
    }
}