package com.github.luoyemyy.aclin.mvp2

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.ext.runOnThread
import java.util.concurrent.atomic.AtomicBoolean

open class ListLiveData<T>(transform: (T) -> DataItem<T>) : MutableLiveData<List<DataItem<T>>>() {

    private val mDataSet = DataSet(transform)
    private val mInitiated = AtomicBoolean(false)
    private val mLoading = AtomicBoolean(false)

    fun config(enableMore: Boolean, reversed: Boolean) {
        mDataSet.also {
            it.enableMore = enableMore
            it.reversed = reversed
        }
        if (mInitiated.compareAndSet(false, true)) {
            postValue(mDataSet.itemList())
        }
    }

    @MainThread
    fun loadStart(startData: List<T>? = null) {
        if (!startData.isNullOrEmpty()) {
            postValue(mDataSet.addStartData(startData))
        } else {
            if (mLoading.compareAndSet(false, true)) {
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

    @MainThread
    fun loadMore() {
        if (mInitiated.get() && mDataSet.isMoreEnd()) {
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

}