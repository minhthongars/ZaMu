package com.minhthong.setting.presentation

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.minhthong.setting.databinding.ViewHolderCutAudioBinding

class CutAudioViewHolder(
    private val binding: ViewHolderCutAudioBinding,
    private val onItemClicked: (Int) -> Unit,
    private val onRemoveItemClick: (Int) -> Unit,
): RecyclerView.ViewHolder(binding.root) {

    fun bind(cutAdapterItem: CutAdapterItem) {
        binding.tvName.text = cutAdapterItem.name
        binding.ivTrackAvatar.setImageBitmap(cutAdapterItem.avatar)
        binding.tvCutInfo.text = cutAdapterItem.cutInfo

        binding.tvOrder.isVisible = cutAdapterItem.order != null
        binding.tvOrder.text = cutAdapterItem.order.toString()

        binding.root.setOnClickListener {
            onItemClicked.invoke(cutAdapterItem.id)
        }

        binding.ivClose.setOnClickListener {
            onRemoveItemClick.invoke(cutAdapterItem.id)
        }
    }
}