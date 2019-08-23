package com.github.luoyemyy.aclin.mvp

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class ListLiveData : LiveData<DataItemGroup>() {

    val refreshLiveData = MutableLiveData<Boolean>()
    val changeLiveData = MutableLiveData<Bundle>() // position & payload

    private val mDataSet by lazy { DataSet() }
    private val mLoadType = LoadType()
    private var mDisposable: Disposable? = null

    fun configDataSet(empty: Boolean, more: Boolean, init: Boolean, moreGone: Boolean) {
        mDataSet.enableEmptyItem = empty
        mDataSet.enableMoreItem = more
        mDataSet.enableInitItem = init
        mDataSet.enableMoreGone = moreGone
    }

    @MainThread
    fun loadInit(bundle: Bundle?) {
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
    fun loadSearch(search: String?) {
        if (mLoadType.search()) {
            loadSearchBefore(search)
            loadDataBase(search = search)
        }
    }

    @MainThread
    fun loadMore() {
        if (mDataSet.canLoadMore() && mLoadType.more()) {
            loadMoreBefore()
            loadDataBase()
        }
    }

    private fun loadDataBase(bundle: Bundle? = null, search: String? = null) {
        if (loadData(bundle, search, mDataSet.paging, mLoadType) { ok, items -> loadDataAfter(ok, items) }) {
            return
        }
        mDisposable = Single.create<List<DataItem>> {
            it.onSuccess(loadData(bundle, search, mDataSet.paging, mLoadType) ?: listOf())
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { items, error ->
            loadDataAfter(error == null, items)
        }
    }

    @MainThread
    open fun loadData(bundle: Bundle? = null, search: String? = null, paging: Paging, loadType: LoadType,
        loadDataAfter: (ok: Boolean, items: List<DataItem>) -> Unit): Boolean = false

    @WorkerThread
    open fun loadData(bundle: Bundle? = null, search: String? = null, paging: Paging, loadType: LoadType): List<DataItem>? = null

    open fun loadInitBefore(bundle: Bundle?) {
        update { DataItemGroup(true, it.setDataLoading()) }
        mDataSet.paging.reset()
    }

    open fun loadRefreshBefore(refreshStyle: Boolean) {
        refreshLiveData.value = refreshStyle
        mDataSet.paging.reset()
    }

    open fun loadSearchBefore(search: String?) {
        update { DataItemGroup(true, it.setDataLoading()) }
        mDataSet.paging.reset()
    }

    open fun loadMoreBefore() {
        mDataSet.paging.next()
    }

    open fun loadMoreAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        return if (ok) {
            mDataSet.addDataSuccess(items)
        } else {
            mDataSet.addDataFailure()
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

    open fun loadDataAfter(ok: Boolean, items: List<DataItem>) {
        when {
            mLoadType.isMore() -> DataItemGroup(false, loadMoreAfter(ok, items))
            mLoadType.isInit() -> DataItemGroup(true, loadInitAfter(ok, items))
            mLoadType.isSearch() -> DataItemGroup(true, loadSearchAfter(ok, items))
            mLoadType.isRefresh() -> DataItemGroup(true, loadRefreshAfter(ok, items))
            else -> null
        }?.also { group -> update { group } }
        mLoadType.complete()
    }

    fun move(start: DataItem?, end: DataItem?): Boolean {
        return update { DataItemGroup(false, it.move(start, end)) }
    }

    /**
     * DataSet#addDataAnchor
     * DataSet#delete
     */
    fun update(callback: (DataSet) -> DataItemGroup?): Boolean {
        return callback(mDataSet)?.let {
            postValue(it)
            true
        } ?: false
    }

    fun change(position: Int, change: (Bundle, DataItem) -> Unit) {
        value?.data?.apply {
            if (position in 0 until size) {
                val bundle = bundleOf("position" to position, "payload" to false)
                change(bundle, this[position])
                changeLiveData.postValue(bundle)
            }
        }
    }

    fun change(change: (Bundle, DataItem) -> Boolean) {
        value?.data?.forEachIndexed { index, dataItem ->
            val bundle = bundleOf("position" to index, "payload" to false)
            if (change(bundle, dataItem)) {
                changeLiveData.postValue(bundle)
            }
        }
    }

    open fun moveEnd() {
    }

    open fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return true
    }
}