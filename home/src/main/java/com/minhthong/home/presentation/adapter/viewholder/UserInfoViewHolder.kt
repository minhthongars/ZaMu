package com.minhthong.home.presentation.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.minhthong.home.databinding.ViewHolderUserInfoBinding
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.adapter.HomeAdapterItem

class UserInfoViewHolder(
    val binding: ViewHolderUserInfoBinding,
    private val listener: HomeAdapterClickListener
): HomeViewHolder(binding.root) {

    companion object {

        fun create(
            parent: ViewGroup,
            listener: HomeAdapterClickListener
        ): HomeViewHolder {
            val binding = ViewHolderUserInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return UserInfoViewHolder(binding, listener)
        }
    }

    override fun bind(data: HomeAdapterItem) {
        val userData = data as? HomeAdapterItem.UserInfo ?: return

        Glide.with(itemView.context)
            .load(userData.avatarUrl)
            .into(binding.ivAvatar)

        binding.tvName.text = userData.name
        binding.tvRank.text = userData.tier

        binding.ivNotification.setOnClickListener {
            listener.onSaveListingClick()
        }
    }

}