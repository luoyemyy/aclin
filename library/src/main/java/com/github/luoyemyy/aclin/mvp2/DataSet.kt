@file:Suppress("unused")

package com.github.luoyemyy.aclin.mvp2

import okhttp3.internal.toImmutableList

class DataSet<T>(
        private val transform: (T) -> DataItem<T>) {

    companion object {
        const val INIT_LOADING = -101
        const val INIT_FAILURE = -102
        const val INIT_EMPTY = -103
        const val INIT_END = -104
        const val MORE_LOADING = -201
        const val MORE_END = -202
        const val MORE_FAILURE = -203
    }

    var enableMore: Boolean = true
    var reversed: Boolean = false
    private val mDataList: MutableList<T> = mutableListOf()
    private val mExtraItem by lazy {
        DataItem<T>()
    }

    /**
     * state流程图
     *
     * init_loading ->  -init_failure   ->  -init_loading
     *                  -init_empty
     *                  -init_end       ->  -more_failure  ->  -more_loading
     *                                      -more_loading
     *                                      -more_end
     */
    private var mState: Int = INIT_LOADING

    fun setStartFail(): List<DataItem<T>> {
        mState = INIT_FAILURE
        return itemList()
    }

    fun addStartData(startData: List<T>?): List<DataItem<T>> {
        mDataList.clear()
        mState = if (startData.isNullOrEmpty()) {
            INIT_EMPTY
        } else {
            mDataList.addAll(startData)
            if (enableMore) MORE_LOADING else INIT_END
        }
        return itemList()
    }

    fun setMoreFail(): List<DataItem<T>> {
        mState = MORE_FAILURE
        return itemList()
    }

    fun addMoreData(moreData: List<T>?): List<DataItem<T>> {
        mState = if (moreData.isNullOrEmpty()) {
            MORE_END
        } else {
            if (reversed) {
                mDataList.addAll(0, moreData)
            } else {
                mDataList.addAll(moreData)
            }
            MORE_LOADING
        }
        return itemList()
    }

    fun itemList(): List<DataItem<T>> {
        val list = mDataList.mapTo(mutableListOf(), transform)
        if (mState != INIT_END) {
            mExtraItem.type = mState
            if (reversed) {
                list.add(0, mExtraItem)
            } else {
                list.add(mExtraItem)
            }
        }
        return list.toImmutableList()
    }

    fun isMoreEnd(): Boolean {
        return mState == MORE_END
    }


    fun dataList(): List<T> = mDataList.toImmutableList()
}