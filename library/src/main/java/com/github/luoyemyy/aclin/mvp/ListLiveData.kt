package com.github.luoyemyy.aclin.mvp

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class ListLiveData : MutableLiveData<DataItemChange>() {

    private val refreshLiveData = MutableLiveData<Boolean>()

    private val mDataSet by lazy { DataSet() }
    private val mLoadType = LoadType()
    private var mDisposable: Disposable? = null

    internal fun observeRefresh(owner: LifecycleOwner, observer: Observer<Boolean>) {
        refreshLiveData.removeObservers(owner)
        refreshLiveData.observe(owner, observer)
    }

    internal fun observeChange(owner: LifecycleOwner, observer: Observer<DataItemChange>) {
        removeObservers(owner)
        observe(owner, observer)
    }

    internal fun configDataSet(empty: Boolean, more: Boolean, init: Boolean, moreGone: Boolean) {
        mDataSet.enableEmptyItem = empty
        mDataSet.enableMoreItem = more
        mDataSet.enableInitItem = init
        mDataSet.enableMoreGone = moreGone
    }

    open fun loadInitBefore(bundle: Bundle?) {
        postValue(DataItemChange(mDataSet.setDataLoading(), true))
        mDataSet.paging.reset()
    }

    open fun loadRefreshBefore(refreshStyle: Boolean) {
        refreshLiveData.value = refreshStyle
        mDataSet.paging.reset()
    }

    open fun loadSearchBefore(search: Bundle?) {
        postValue(DataItemChange(mDataSet.setDataLoading(), true))
        mDataSet.paging.reset()
    }

    open fun loadMoreBefore() {
        mDataSet.paging.next()
    }

    @MainThread
    fun loadInit(bundle: Bundle? = null) {
        if (mDataSet.canLoadInit() && mLoadType.init()) {
            loadInitBefore(bundle)
            loadDataBase(bundle)
        }
    }

    @MainThread
    fun loadRefresh(refreshStyle: Boolean = true) {
        if (mLoadType.refresh()) {
            loadRefreshBefore(refreshStyle)
            loadDataBase()
        } else {
            refreshLiveData.value = false
        }
    }

    @MainThread
    fun loadSearch(search: Bundle? = null) {
        if (mLoadType.search()) {
            loadSearchBefore(search)
            loadDataBase(search)
        }
    }

    @MainThread
    fun loadMore() {
        if (mDataSet.canLoadMore() && mLoadType.more()) {
            loadMoreBefore()
            loadDataBase()
        }
    }

    open fun loadInitAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        return if (ok) {
            mDataSet.setDataSuccess(items)
        } else {
            mDataSet.setDataFailure()
        }
    }

    open fun loadRefreshAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        refreshLiveData.value = false
        return if (ok) {
            mDataSet.setDataSuccess(items)
        } else {
            mDataSet.setDataFailure()
        }
    }

    open fun loadSearchAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        return if (ok) {
            mDataSet.setDataSuccess(items)
        } else {
            mDataSet.setDataFailure()
        }
    }

    open fun loadMoreAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        return if (ok) {
            mDataSet.addDataSuccess(items)
        } else {
            mDataSet.paging.errorBack()
            mDataSet.addDataFailure()
        }
    }

    open fun loadDataAfter(ok: Boolean, items: List<DataItem>) {
        when {
            mLoadType.isInit() -> postValue(DataItemChange(loadInitAfter(ok, items), true))
            mLoadType.isRefresh() -> postValue(DataItemChange(loadRefreshAfter(ok, items), true))
            mLoadType.isSearch() -> postValue(DataItemChange(loadSearchAfter(ok, items), true))
            mLoadType.isMore() -> postValue(DataItemChange(loadMoreAfter(ok, items), false))
        }
        mLoadType.complete()
    }

    private fun loadDataBase(bundle: Bundle? = null) {
        if (loadData(bundle, mDataSet.paging, mLoadType) { ok, items -> loadDataAfter(ok, items) }) {
            return
        }
        mDisposable = Single
            .create<List<DataItem>> {
                it.onSuccess(loadData(bundle, mDataSet.paging, mLoadType) ?: listOf())
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { items, error ->
                loadDataAfter(error == null, items)
            }
    }

    @MainThread
    open fun loadData(bundle: Bundle? = null, paging: Paging, loadType: LoadType, loadDataAfter: LoadDataAfter<DataItem>): Boolean = false

    @WorkerThread
    open fun loadData(bundle: Bundle? = null, paging: Paging, loadType: LoadType): List<DataItem>? = null

    fun itemSortMove(start: Int, end: Int): Boolean {
        return value?.data?.let {
            val range = 0 until it.size
            val startItem = if (start in range) it[start] else null
            val endItem = if (end in range) it[end] else null
            mDataSet.move(startItem, endItem)?.let { list ->
                postValue(DataItemChange(list))
                true
            } ?: false
        } ?: false
    }

    open fun itemSortEnd() {}

    fun itemChange(change: ItemCallback = { _, _ -> true }) {
        itemUpdateBase(change)
    }

    fun itemDelete(delete: ItemCallback = { _, _ -> true }) {
        itemUpdateBase(delete)
    }

    private fun itemUpdateBase(base: ItemCallback) {
        val items = value?.data
        if (base(items, mDataSet)) {
            postValue(DataItemChange(mDataSet.getDataList()))
        }
    }
}