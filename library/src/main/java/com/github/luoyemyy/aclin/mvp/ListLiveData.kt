package com.github.luoyemyy.aclin.mvp

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.ext.runOnThread
import java.util.concurrent.atomic.AtomicBoolean

open class ListLiveData<T>(transform: (T) -> DataItem<T>) : MutableLiveData<List<DataItem<T>>>() {

    private val mDataSet = DataSet(transform)
    private val mLoadStart = AtomicBoolean(false)
    private val mLoading = AtomicBoolean(false)

    init {
        value = mDataSet.itemList()
    }

    fun config(enableMore: Boolean, reversed: Boolean) {
        mDataSet.also {
            it.enableMore = enableMore
            it.reversed = reversed
        }
    }

    @MainThread
    fun loadStart(startData: List<T>? = null, forceLoad: Boolean = false) {
        if (mLoadStart.compareAndSet(false, true) || forceLoad) {
            if (mLoading.compareAndSet(false, true)) {
                if (!startData.isNullOrEmpty()) {
                    postValue(mDataSet.addStartData(startData))
                    mLoading.set(false)
                } else {
                    runOnThread {
                        try {
                            postValue(mDataSet.addStartData(getStartData()))
                        } catch (e: Throwable) {
                            postValue(mDataSet.setStartFail())
                        } finally {
                            mLoading.set(false)
                        }
                    }
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
                    postValue(mDataSet.addMoreData(getMoreData()))
                } catch (e: Throwable) {
                    postValue(mDataSet.setMoreFail())
                } finally {
                    mLoading.set(false)
                }
            }
        }
    }

    @WorkerThread
    open fun getStartData(): List<T>? {
        return null
    }

    @WorkerThread
    open fun getMoreData(): List<T>? {
        return null
    }

    fun itemSortMove(start: Int, end: Int): Boolean {
        return mDataSet.move(start, end)?.let {
            postValue(it)
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
            postValue(mDataSet.itemList())
        }
    }

}