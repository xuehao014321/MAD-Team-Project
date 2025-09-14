package com.example.mad_gruop_ass

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class StaggeredSpacingItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        val spanIndex = layoutParams.spanIndex
        
        // 左右间距
        when (spanIndex) {
            0 -> {
                // 左列
                outRect.left = spacing
                outRect.right = spacing / 2
            }
            1 -> {
                // 右列
                outRect.left = spacing / 2
                outRect.right = spacing
            }
        }
        
        // 顶部间距 - 小红书风格的不规则间距
        outRect.top = if (parent.getChildAdapterPosition(view) < 2) {
            spacing
        } else {
            spacing / 2
        }
        
        // 底部间距
        outRect.bottom = spacing / 2
    }
}




