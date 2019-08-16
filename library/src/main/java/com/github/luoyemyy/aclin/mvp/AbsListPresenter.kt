package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class AbsListPresenter(app: Application) : AndroidViewModel(app) {

    val itemList = MutableLiveData<List<DataItem>>()
    val refreshState = MutableLiveData<Boolean>()
    val changePosition = MutableLiveData<Int>()

    private val mDataSet by lazy { DataSet() }
    private val mLoadType = LoadType()
    private var mDisposable: Disposable? = null

    fun configDataSet(empty: Boolean, more: Boolean, moreGone: Boolean) {
        mDataSet.enableEmpty = empty
        mDataSet.enableMore = more
        mDataSet.enableMoreGone = moreGone
    }

    @MainThread
    fun loadInit(bundle: Bundle?) {
        if (mLoadType.init()) {
            loadInitBefore(bundle)
            loadDataBase(bundle)
        }
    }

    @MainThread
    fun loadRefresh() {
        if (mLoadType.refresh()) {
            loadRefreshBefore()
            loadDataBase()
        } else {
            refreshState.value = false
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
    open fun loadData(
        bundle: Bundle? = null, search: String? = null, paging: Paging, loadType: LoadType,
        loadDataAfter: (ok: Boolean, items: List<DataItem>) -> Unit
    ): Boolean = false

    @WorkerThread
    open fun loadData(bundle: Bundle? = null, search: String? = null, paging: Paging, loadType: LoadType): List<DataItem>? = null

    open fun loadInitBefore(bundle: Bundle?) {
        update { it.setDataLoading() }
        mDataSet.paging.reset()
    }

    open fun loadRefreshBefore() {
        refreshState.value = true
        mDataSet.paging.reset()
    }

    open fun loadSearchBefore(search: String?) {
        update { it.setDataLoading() }
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
        refreshState.value = false
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
            mLoadType.isMore() -> loadMoreAfter(ok, items)
            mLoadType.isInit() -> loadInitAfter(ok, items)
            mLoadType.isSearch() -> loadSearchAfter(ok, items)
            mLoadType.isRefresh() -> loadRefreshAfter(ok, items)
            else -> null
        }?.also { list -> update { list } }
        mLoadType.complete()
    }

    fun move(start: DataItem?, end: DataItem?): Boolean {
        return update { it.move(start, end) }
    }

    /**
     * DataSet#addDataAnchor
     * DataSet#delete
     */
    fun update(callback: (DataSet) -> List<DataItem>?): Boolean {
        return callback(mDataSet)?.let {
            itemList.postValue(it)
            true
        } ?: false
    }

    fun change(dataItem: DataItem) {
        (itemList.value?.indexOf(dataItem) ?: -1).also {
            if (it >= 0) {
                changePosition.postValue(it)
            }
        }
    }

    open fun moveEnd() {
    }

    open fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return true
    }
}