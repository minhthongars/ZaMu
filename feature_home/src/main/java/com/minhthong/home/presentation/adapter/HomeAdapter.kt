package com.minhthong.home.presentation.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.minhthong.home.presentation.adapter.viewholder.ErrorViewHolder
import com.minhthong.home.presentation.adapter.viewholder.HomeViewHolder
import com.minhthong.home.presentation.adapter.viewholder.LoadingViewHolder
import com.minhthong.home.presentation.adapter.viewholder.RemoteTrackViewHolder
import com.minhthong.home.presentation.adapter.viewholder.TitleViewHolder
import com.minhthong.home.presentation.adapter.viewholder.TrackViewHolder
import com.minhthong.home.presentation.adapter.viewholder.UserInfoViewHolder

class HomeAdapter(
    private val listener: HomeAdapterClickListener,
): ListAdapter<HomeAdapterItem, HomeViewHolder>(ItemCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeViewHolder {
        return when(viewType) {
            HomeAdapterItem.ViewType.USER_INFO -> UserInfoViewHolder.create(parent, listener)

            HomeAdapterItem.ViewType.TITLE -> TitleViewHolder.create(parent)

            HomeAdapterItem.ViewType.TRACK -> TrackViewHolder.create(parent, listener)

            HomeAdapterItem.ViewType.REMOTE_TRACK -> RemoteTrackViewHolder.create(parent, listener)

            HomeAdapterItem.ViewType.LOADING_VIEW -> LoadingViewHolder.create(parent)

            HomeAdapterItem.ViewType.ERROR_VIEW -> ErrorViewHolder.create(parent, listener)

            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(
        holder: HomeViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int, payloads: List<Any?>) {
        val payload = payloads.firstOrNull()
        val item = getItem(position)

        when(payload) {
            HomeAdapterItem.PlayLoad.ADDING_TO_PLAYLIST -> {
                val isLoading = (item as? HomeAdapterItem.Track)?.isLoading == true
                (holder as? TrackViewHolder)?.bindLoading(isLoading)
            }

            else -> {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    override fun onViewAttachedToWindow(holder: HomeViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is LoadingViewHolder) {
            holder.startShimmer()
        }
    }

    override fun onViewDetachedFromWindow(holder: HomeViewHolder) {
        if (holder is LoadingViewHolder) {
            holder.stopShimmer()
        }
        super.onViewDetachedFromWindow(holder)
    }

    private class ItemCallback: DiffUtil.ItemCallback<HomeAdapterItem>() {
        override fun areItemsTheSame(
            oldItem: HomeAdapterItem,
            newItem: HomeAdapterItem
        ): Boolean {
            return oldItem.areItemsTheSame(newItem)
        }

        override fun areContentsTheSame(
            oldItem: HomeAdapterItem,
            newItem: HomeAdapterItem
        ): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }

        override fun getChangePayload(oldItem: HomeAdapterItem, newItem: HomeAdapterItem): Any? {
            return oldItem.getChangePayload(newItem)
        }
    }
}