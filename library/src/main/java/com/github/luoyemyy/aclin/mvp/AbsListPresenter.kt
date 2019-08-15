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

    @MainThread
    fun loadInit(bundle: Bundle?) {
        if (mLoadType.init()) {
            update {
                it.setDataLoading()
            }
            mDataSet.paging.reset()
            loadDataAsync(bundle)
        }
    }

    @MainThread
    fun loadRefresh() {
        if (mLoadType.refresh()) {
            refreshState.value = true
            mDataSet.paging.reset()
            loadDataAsync()
        } else {
            refreshState.value = false
        }
    }

    @MainThread
    fun loadSearch(search: String?) {
        if (mLoadType.search()) {
            update {
                it.setDataLoading()
            }
            mDataSet.paging.reset()
            loadDataAsync(search = search)
        }
    }

    @MainThread
    fun loadMore() {
        if (mDataSet.canLoadMore() && mLoadType.more()) {
            mDataSet.paging.next()
            loadDataAsync()
        }
    }

    private fun loadDataAsync(bundle: Bundle? = null, search: String? = null) {
        mDisposable = Single.create<List<DataItem>> {
            it.onSuccess(loadData(bundle, search, mDataSet.paging, mLoadType) ?: listOf())
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe { items, error ->
            loadDataAfter(error == null, items)
        }
    }

    @WorkerThread
    abstract fun loadData(bundle: Bundle? = null, search: String? = null, paging: Paging = mDataSet.paging, loadType: LoadType = mLoadType): List<DataItem>?

    private fun loadDataAfter(ok: Boolean, items: List<DataItem>) {
        update {
            when {
                mLoadType.isMore() -> {
                    if (ok) {
                        it.addDataSuccess(items)
                    } else {
                        it.addDataFailure()
                    }
                }
                else -> {
                    if (ok) {
                        it.setDataSuccess(items)
                    } else {
                        it.setDataFailure()
                    }
                }
            }
        }
        if (mLoadType.isRefresh()) {
            refreshState.value = false
        }
        mLoadType.complete()
    }

    fun move(start: DataItem?, end: DataItem?): Boolean {
        return update {
            it.move(start, end)
        }
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
        val index = itemList.value?.let {
            it.indexOf(dataItem)
        } ?: -1
        if (index >= 0) {
            changePosition.postValue(index)
        }
    }

    fun configDataSet(empty: Boolean, more: Boolean, moreGone: Boolean) {
        mDataSet.enableEmpty = empty
        mDataSet.enableMore = more
        mDataSet.enableMoreGone = moreGone
    }

    open fun moveEnd() {
    }

    open fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return true
    }
}