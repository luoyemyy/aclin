package com.github.luoyemyy.aclin.mvp

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.ext.runOnThread
import java.util.concurrent.atomic.AtomicBoolean

open class ListLiveData<T>(transform: (T) -> DataItem<T>) : MutableLiveData<NotifyData<T>>() {

    private val mDataSet = DataSet(transform)
    private val mLoadStart = AtomicBoolean(false)
    private val mLoading = AtomicBoolean(false)
    private val mLoadParams = LoadParams()

    init {
        value = NotifyData(LoadParams.TYPE_INIT, mDataSet.itemList())
    }

    fun config(enableMore: Boolean, reversed: Boolean) {
        mDataSet.also {
            it.enableMore = enableMore
            it.reversed = reversed
        }
    }

    private fun post(@LoadParams.LoadType type: Int, items: List<DataItem<T>>) {
        postValue(NotifyData(type, items))
    }

    @MainThread
    fun loadRefresh(refreshData: List<T>? = null) {
        if (mLoading.compareAndSet(false, true)) {
            mLoadStart.set(true)
            if (!refreshData.isNullOrEmpty()) {
                post(mLoadParams.loadType, mDataSet.addStartData(refreshData))
                mLoading.set(false)
                return
            }
            runOnThread {
                try {
                    mLoadParams.refresh()
                    mLoadParams.resetPage()
                    post(mLoadParams.loadType, mDataSet.addStartData(getData(mLoadParams)))
                } catch (e: Throwable) {
                    mLoadParams.backPage()
                } finally {
                    mLoading.set(false)
                }
            }
        }
    }

    @MainThread
    fun loadStart(startData: List<T>? = null) {
        if (mLoadStart.compareAndSet(false, true) && mLoading.compareAndSet(false, true)) {
            if (!startData.isNullOrEmpty()) {
                post(mLoadParams.loadType, mDataSet.addStartData(startData))
                mLoading.set(false)
                return
            }
            runOnThread {
                try {
                    mLoadParams.start()
                    mLoadParams.resetPage()
                    post(mLoadParams.loadType, mDataSet.addStartData(getData(mLoadParams)))
                } catch (e: Throwable) {
                    mLoadParams.backPage()
                    post(mLoadParams.loadType, mDataSet.setStartFail())
                } finally {
                    mLoading.set(false)
                }
            }
        }
    }

    @MainThread
    fun loadMore() {
        if (!mLoadStart.get() || mDataSet.isMoreEnd()) { //没有加载过第一页数据 或者 已加载全部数据 ，则跳过
            return
        }
        if (mLoading.compareAndSet(false, true)) {
            runOnThread {
                try {
                    mLoadParams.more()
                    mLoadParams.nextPage()
                    post(mLoadParams.loadType, mDataSet.addMoreData(getData(mLoadParams)))
                } catch (e: Throwable) {
                    mLoadParams.backPage()
                    post(mLoadParams.loadType, mDataSet.setMoreFail())
                } finally {
                    mLoading.set(false)
                }
            }
        }
    }

    @WorkerThread
    open fun getData(loadParams: LoadParams): List<T>? {
        return null
    }

    fun itemSortMove(start: Int, end: Int): Boolean {
        return mDataSet.move(start, end)?.let {
            post(LoadParams.TYPE_UPDATE, it)
            true
        } ?: false
    }

    open fun itemSortEnd() {}

    fun itemList(): List<DataItem<T>>? {
        return mDataSet.lastItemList
    }

    fun dataList(): MutableList<T> {
        return mDataSet.dataList
    }

    fun itemChange(callback: (itemList: List<DataItem<T>>?, dataList: MutableList<T>) -> Boolean) {
        if (callback(itemList(), dataList())) {
            post(LoadParams.TYPE_UPDATE, mDataSet.itemList())
        }
    }

}