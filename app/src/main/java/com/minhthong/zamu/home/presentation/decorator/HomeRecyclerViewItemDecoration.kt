package com.minhthong.zamu.home.presentation.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.minhthong.zamu.home.presentation.adapter.HomeAdapterItem

class HomeRecyclerViewItemDecoration(
    private val verticalSpace: Int,
    private val horizontalSpace: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        outRect.bottom = verticalSpace
        outRect.left = horizontalSpace
        outRect.right = horizontalSpace

        val viewType = parent.adapter?.getItemViewType(position)
        if (viewType == HomeAdapterItem.ViewType.TITLE) {
            outRect.top = verticalSpace
        }
    }
}