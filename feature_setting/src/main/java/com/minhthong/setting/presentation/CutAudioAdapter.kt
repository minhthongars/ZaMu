package com.minhthong.setting.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.minhthong.setting.databinding.ViewHolderCutAudioBinding

class CutAudioAdapter(
    private val onItemClicked: (Int) -> Unit,
    private val onRemoveItemClick: (Int) -> Unit,
): ListAdapter<CutAdapterItem, CutAudioViewHolder>(
    object : DiffUtil.ItemCallback<CutAdapterItem>() {
        override fun areItemsTheSame(
            oldItem: CutAdapterItem,
            newItem: CutAdapterItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CutAdapterItem,
            newItem: CutAdapterItem
        ): Boolean {
            return oldItem == newItem
        }

    }
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CutAudioViewHolder {
        return CutAudioViewHolder(
            ViewHolderCutAudioBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClicked,
            onRemoveItemClick
        )
    }

    override fun onBindViewHolder(
        holder: CutAudioViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}