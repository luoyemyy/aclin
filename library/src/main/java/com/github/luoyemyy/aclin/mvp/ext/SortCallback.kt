package com.github.luoyemyy.aclin.mvp.ext

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.mvp.core.MvpData
import com.github.luoyemyy.aclin.mvp.core.ListLiveData

class SortCallback<T : MvpData>(private val mLiveData: ListLiveData<T>) :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return mLiveData.itemSortMove(viewHolder.adapterPosition, target.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            mLiveData.itemSortEnd()
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}