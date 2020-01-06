package com.github.luoyemyy.aclin.mvp.core

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.ext.runOnThread
import java.util.concurrent.atomic.AtomicBoolean

open class ListLiveData<T : MvpData>(transform: (T) -> DataItem<T>) : MutableLiveData<NotifyItems<T>>() {

    private val mDataSet = DataSet(transform)
    private val mInit = AtomicBoolean(false)
    private val mLoadStart = AtomicBoolean(false)
    private val mLoading = AtomicBoolean(false)
    private val mLoadParams = LoadParams()

    fun enableMore(enable: Boolean) {
        mDataSet.enableMore = enable
    }

    fun enableInit(enable: Boolean) {
        mDataSet.enableInit = enable
    }

    fun reversed(enable: Boolean) {
        mDataSet.reversed = enable
    }

    fun startInit() {
        if (mInit.compareAndSet(false, true)) {
            post(LoadParams.TYPE_INIT, mDataSet.itemList())
        }
    }

    private fun post(@LoadParams.LoadType type: Int, items: List<DataItem<T>>) {
        postValue(NotifyItems(type, items))
    }

    @MainThread
    fun loadRefresh(refreshData: List<T>? = null) {
        if (mLoading.compareAndSet(false, true)) {
            mLoadStart.set(true)
            mLoadParams.refresh()
            if (!refreshData.isNullOrEmpty()) {
                post(mLoadParams.loadType, mDataSet.addStartData(refreshData))
                mLoading.set(false)
                return
            }
            runOnThread {
                try {
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
    fun loadStart(startData: List<T>? = null, reload: Boolean = false) {
        if (((reload && mLoading.get()) || mLoadStart.compareAndSet(false, true)) && mLoading.compareAndSet(false, true)) {
            if (reload) {
                mDataSet.setStartLoading()
                post(LoadParams.TYPE_UPDATE, mDataSet.itemList())
            }
            mLoadParams.start()
            if (!startData.isNullOrEmpty()) {
                post(mLoadParams.loadType, mDataSet.addStartData(startData))
                mLoading.set(false)
                return
            }
            runOnThread {
                try {
                    post(mLoadParams.loadType, mDataSet.addStartData(getData(mLoadParams)))
                } catch (e: Throwable) {
                    post(mLoadParams.loadType, mDataSet.setStartFail())
                } finally {
                    mLoading.set(false)
                }
            }
        }
    }

    @MainThread
    fun loadMore(reload: Boolean = false) {
        if (!mLoadStart.get() || mDataSet.isMoreEnd()) { //没有加载过第一页数据 或者 已加载全部数据 ，则跳过
            return
        }
        if (mLoading.compareAndSet(false, true)) {
            if (reload) {
                mDataSet.setMoreLoading()
                post(LoadParams.TYPE_UPDATE, mDataSet.itemList())
            }
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