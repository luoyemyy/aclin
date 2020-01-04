package com.github.luoyemyy.aclin.mvp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

typealias UpdateListener<T> = (oldList: List<DataItem<T>>?, newList: List<DataItem<T>>?) -> Unit

inline fun <reified T : AndroidViewModel> FragmentActivity.getPresenter(): T = ViewModelProviders.of(this).get(T::class.java)

inline fun <reified T : AndroidViewModel> Fragment.getPresenter(): T = ViewModelProviders.of(this).get(T::class.java)

fun RecyclerView.setupLinear(adapter: RecyclerView.Adapter<*>,
                             vertical: Boolean = true,
                             decoration: RecyclerView.ItemDecoration = LinearDecoration.middle(context)) {
    this.layoutManager = LinearLayoutManager(context, if (vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL, false)
    this.adapter = adapter
    this.addItemDecoration(decoration)
}

fun RecyclerView.setupGrid(adapter: RecyclerView.Adapter<*>,
                           span: Int,
                           vertical: Boolean = true,
                           decoration: RecyclerView.ItemDecoration = GridDecoration.create(context, span, 1)) {
    this.layoutManager = StaggeredGridLayoutManager(span, if (vertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL)
    this.adapter = adapter
    this.addItemDecoration(decoration)
}

fun <T : MvpData> SwipeRefreshLayout.setup(listLiveData: ListLiveData<T>) {
    this.setOnRefreshListener {
        listLiveData.loadStart()
    }
}
