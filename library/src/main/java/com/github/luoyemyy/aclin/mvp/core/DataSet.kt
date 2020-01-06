@file:Suppress("unused")

package com.github.luoyemyy.aclin.mvp.core

import androidx.annotation.IntDef
import java.util.*

class DataSet<T : MvpData>(private val mTransform: (T) -> DataItem<T>) {

    companion object {
        const val INIT_LOADING = -101
        const val INIT_FAILURE = -102
        const val INIT_EMPTY = -103
        const val INIT_END = -104
        const val MORE_LOADING = -201
        const val MORE_END = -202
        const val MORE_FAILURE = -203
    }

    @IntDef(INIT_LOADING, INIT_FAILURE, INIT_EMPTY, INIT_END, MORE_LOADING, MORE_END, MORE_FAILURE)
    annotation class State

    var enableInit: Boolean = true
    var enableMore: Boolean = true
    var reversed: Boolean = false
    val dataList: MutableList<T> = mutableListOf()
    var lastItemList: List<DataItem<T>>? = null
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
    @State
    private var mState: Int = INIT_LOADING

    fun setStartLoading(): List<DataItem<T>> {
        mState = INIT_LOADING
        return itemList()
    }

    fun setStartFail(): List<DataItem<T>> {
        mState = INIT_FAILURE
        return itemList()
    }

    fun addStartData(startData: List<T>?): List<DataItem<T>> {
        dataList.clear()
        mState = if (startData.isNullOrEmpty()) {
            INIT_EMPTY
        } else {
            dataList.addAll(startData)
            if (enableMore) MORE_LOADING else INIT_END
        }
        return itemList()
    }

    fun setMoreLoading(): List<DataItem<T>> {
        mState = MORE_LOADING
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
                dataList.addAll(0, moreData)
            } else {
                dataList.addAll(moreData)
            }
            MORE_LOADING
        }
        return itemList()
    }

    fun itemList(): List<DataItem<T>> {
        val list = mutableListOf<DataItem<T>>()
        dataList.forEach {
            val item = it.dataItem as? DataItem<T>
            if (item != null) {
                list.add(item)
            } else {
                mTransform(it).apply {
                    it.dataItem = this
                    list.add(this)
                }
            }
        }
        if (mState != INIT_END) {
            mExtraItem.type = mState
            if (mState !in arrayOf(INIT_LOADING, INIT_EMPTY) || enableInit) {
                if (reversed) {
                    list.add(0, mExtraItem)
                } else {
                    list.add(mExtraItem)
                }
            }
        }
        return list.apply {
            lastItemList = this
        }
    }

    fun isMoreEnd(): Boolean {
        return mState == MORE_END
    }

    fun move(start: Int, end: Int): List<DataItem<T>>? {
        if (start == end) {
            return null
        }
        val startData = lastItemList?.getOrNull(start)?.data ?: return null
        val endData = lastItemList?.getOrNull(end)?.data ?: return null
        val startPosition = dataList.indexOf(startData)
        val endPosition = dataList.indexOf(endData)
        if (startPosition <= -1 || endPosition <= -1) {
            return null
        }
        if (startPosition < endPosition) {
            (startPosition until endPosition).forEach {
                Collections.swap(dataList, it, it + 1)
            }
        } else if (startPosition > endPosition) {
            (startPosition downTo endPosition + 1).forEach {
                Collections.swap(dataList, it, it - 1)
            }
        }
        return itemList()
    }
}