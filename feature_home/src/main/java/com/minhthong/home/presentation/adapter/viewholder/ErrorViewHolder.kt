package com.minhthong.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.minhthong.home.databinding.ViewHolderErrorBinding
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class ErrorViewHolder(
    private val binding: ViewHolderErrorBinding,
    private val listener: HomeAdapterClickListener
): HomeViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            listener: HomeAdapterClickListener
        ): ErrorViewHolder {
            return ErrorViewHolder(
                binding = ViewHolderErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                listener = listener
            )
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val errorData = data as? HomeAdapterItem.ErrorView ?: return

        val layoutParams = itemView.layoutParams
        layoutParams.height = itemView.resources.getDimensionPixelSize(errorData.viewHeight)
        itemView.layoutParams = layoutParams

        binding.tvMessage.text = itemView.context.getText(errorData.message)
        binding.layoutMessage.setOnClickListener {
            listener.onRetryClick(errorData.type)
        }
    }
}