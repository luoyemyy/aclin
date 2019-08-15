package com.github.luoyemyy.aclin.mvp

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil
import kotlin.math.roundToInt

class GridDecoration private constructor(val context: Context, private val mColumn: Int, space: Int, spacePxUnit: Boolean) : RecyclerView.ItemDecoration() {

    companion object {
        fun create(context: Context, column: Int = 3, space: Int = 8, spacePxUnit: Boolean = false): GridDecoration {
            return GridDecoration(context, column, space, spacePxUnit)
        }
    }

    private var mSpacePx = if (spacePxUnit) space else dp2px(space)
    private var mHalfSpacePx = mSpacePx / 2

    private fun dp2px(dp: Int): Int = (context.resources.displayMetrics.density * dp).roundToInt()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemCount = parent.adapter?.itemCount ?: return
        val position = parent.getChildAdapterPosition(view)
        //left
        if (position % mColumn == 0) {
            outRect.left = mSpacePx
        } else {
            outRect.left = mHalfSpacePx
        }
        //top
        if (position < mColumn) {
            outRect.top = mSpacePx
        } else {
            outRect.top = mHalfSpacePx
        }
        //right
        if (position % mColumn == mColumn - 1) {
            outRect.right = mSpacePx
        } else {
            outRect.right = mHalfSpacePx
        }
        //bottom
        if (ceil(position.toDouble() / mColumn.toDouble()).toInt() == ceil(itemCount.toDouble() / mColumn.toDouble()).toInt()) {
            outRect.bottom = mSpacePx
        } else {
            outRect.bottom = mHalfSpacePx
        }
    }
}