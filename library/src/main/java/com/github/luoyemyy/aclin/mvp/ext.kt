package com.github.luoyemyy.aclin.mvp

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

inline fun <reified T : AndroidViewModel> FragmentActivity.getPresenter(): T =
    ViewModelProviders.of(this).get(T::class.java)

fun RecyclerView.setupLinear(adapter: AbsListAdapter, vertical: Boolean = true) {
    this.layoutManager =
        LinearLayoutManager(context, if (vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL, false)
    this.adapter = adapter
    this.addItemDecoration(LinearDecoration.middle(context))
}

fun SwipeRefreshLayout.setup(presenter: AbsListPresenter) {
    this.setOnRefreshListener {
        presenter.loadRefresh()
    }
}