package com.minhthong.zamu.home.presentation.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterItem

abstract class HomeViewHolder(
    view: View
): RecyclerView.ViewHolder(view) {

    abstract fun bind(data: HomeAdapterItem)
}