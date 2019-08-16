package com.github.luoyemyy.aclin.mvp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

inline fun <reified T : AndroidViewModel> FragmentActivity.getPresenter(): T = ViewModelProviders.of(this).get(T::class.java)

inline fun <reified T : AndroidViewModel> Fragment.getPresenter(): T = ViewModelProviders.of(this).get(T::class.java)

fun RecyclerView.setupLinear(adapter: AbsListAdapter, vertical: Boolean = true, decoration: RecyclerView.ItemDecoration = LinearDecoration.middle(context)) {
    this.layoutManager = LinearLayoutManager(context, if (vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL, false)
    this.adapter = adapter
    this.addItemDecoration(decoration)
}

fun SwipeRefreshLayout.setup(presenter: AbsListPresenter) {
    this.setOnRefreshListener {
        presenter.loadRefresh()
    }
}