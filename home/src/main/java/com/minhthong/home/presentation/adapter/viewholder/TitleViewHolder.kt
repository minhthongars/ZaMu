package com.minhthong.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.minhthong.home.databinding.ViewHolderTitleBinding
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class TitleViewHolder(
    private val binding: ViewHolderTitleBinding
): HomeViewHolder(binding.root) {

    companion object {

        fun create(parent: ViewGroup): HomeViewHolder {
            val binding = ViewHolderTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TitleViewHolder(binding)
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val content = (data as? HomeAdapterItem.Title)?.content
        binding.title.text = content
    }

}