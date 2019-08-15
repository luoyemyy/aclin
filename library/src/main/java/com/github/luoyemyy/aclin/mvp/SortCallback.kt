package com.github.luoyemyy.aclin.mvp

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SortCallback(private val mPresenter: AbsListPresenter, private val mAdapter: AbsListAdapter) :
    ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        0
    ) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return mPresenter.move(mAdapter.getItem(viewHolder.adapterPosition), mAdapter.getItem(target.adapterPosition))
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            mPresenter.moveEnd()
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}