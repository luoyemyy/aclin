package com.github.luoyemyy.aclin.mvp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

typealias LoadDataAfter<T> = (ok: Boolean, items: List<T>) -> Unit

typealias ItemCallback = (List<DataItem>?, DataSet) -> Boolean

fun <T : DataItem> getDiffCallback() = object : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return newItem.areItemsTheSame(oldItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return newItem.areContentsTheSame(oldItem)
    }

    override fun getChangePayload(oldItem: T, newItem: T): Any? {
        return newItem.getChangePayload(oldItem)
    }
}

inline fun <reified T : AndroidViewModel> FragmentActivity.getPresenter(): T = defaultViewModelProviderFactory.create(T::class.java)

inline fun <reified T : AndroidViewModel> Fragment.getPresenter(): T = defaultViewModelProviderFactory.create(T::class.java)

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

fun SwipeRefreshLayout.setup(listLiveData: ListLiveData) {
    this.setOnRefreshListener {
        listLiveData.loadRefresh()
    }
}
