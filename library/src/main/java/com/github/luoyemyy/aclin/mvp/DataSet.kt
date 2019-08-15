package com.github.luoyemyy.aclin.mvp

import java.util.*

class DataSet(
    var paging: Paging = Paging.Page(),
    var enableEmpty: Boolean = true,
    var enableMore: Boolean = true,
    var enableMoreGone: Boolean = false
) {

    companion object {
        const val INIT_LOADING = -1
        const val INIT_FAILURE = -2
        const val INIT_END = -3
        const val EMPTY = -201
        const val MORE_LOADING = -101
        const val MORE_END = -102
        const val MORE_FAILURE = -103
        const val CONTENT = 1
    }

    private val mInitLoadingData = DataItem(INIT_LOADING)
    private val mInitFailureData = DataItem(INIT_FAILURE)
    private val mEmptyData = DataItem(EMPTY)
    private val mMoreLoadingData = DataItem(MORE_LOADING)
    private val mMoreEndData = DataItem(MORE_END)
    private val mMoreFailureData = DataItem(MORE_FAILURE)

    /**
     * 内容列表
     */
    private val mData: MutableList<DataItem> = mutableListOf()
    private var mInitState = INIT_LOADING
    private var mMoreState = MORE_LOADING

    /**
     * 判断是否可以加载更多
     */
    fun canLoadMore(): Boolean {
        return enableMore
                && mInitState == INIT_END
                && mMoreState in arrayOf(MORE_LOADING, MORE_FAILURE)
    }

    private fun setLoadMoreState(list: List<DataItem>?) {
        if (enableMore) {
            mMoreState = if (list.isNullOrEmpty() || list.size < paging.size()) {
                MORE_END
            } else {
                MORE_LOADING
            }
        }
    }

    fun setDataLoading(): List<DataItem> {
        mInitState = INIT_LOADING
        return getDataList()
    }

    fun setDataFailure(): List<DataItem> {
        mInitState = INIT_FAILURE
        return getDataList()
    }

    fun setDataSuccess(list: List<DataItem>?): List<DataItem> {
        mData.clear()
        if (!list.isNullOrEmpty()) {
            mData.addAll(list)
        }
        mInitState = INIT_END
        setLoadMoreState(list)
        return getDataList()
    }

    fun addDataFailure(): List<DataItem> {
        mMoreState = MORE_FAILURE
        return getDataList()
    }

    fun addDataSuccess(list: List<DataItem>?): List<DataItem> {
        if (!list.isNullOrEmpty()) {
            mData.addAll(list)
        }
        setLoadMoreState(list)
        return getDataList()
    }

    fun addDataAnchor(anchor: DataItem?, data: DataItem): List<DataItem> {
        return addDataAnchor(anchor, listOf(data))
    }

    fun addDataAnchor(anchor: DataItem?, list: List<DataItem>?): List<DataItem> {
        if (!list.isNullOrEmpty()) {
            val index = anchor?.let {
                mData.indexOf(anchor)
            } ?: -1
            mData.addAll(if (index == -1) 0 else index, list)
        }
        return getDataList()
    }

    fun delete(data: DataItem): List<DataItem> {
        return delete(listOf(data))
    }

    fun delete(list: List<DataItem>?): List<DataItem> {
        if (!list.isNullOrEmpty()) {
            mData.removeAll(list)
        }
        return getDataList()
    }

    fun move(start: DataItem?, end: DataItem?): List<DataItem>? {
        if (start == null || end == null || start == end || start.type <= 0 || end.type <= 0) {
            return null
        }
        val startPosition = mData.indexOf(start)
        val endPosition = mData.indexOf(end)
        if (startPosition <= -1 || endPosition <= -1) {
            return null
        }
        if (startPosition < endPosition) {
            (startPosition until endPosition).forEach {
                Collections.swap(mData, it, it + 1)
            }
        } else if (startPosition > endPosition) {
            (startPosition downTo endPosition + 1).forEach {
                Collections.swap(mData, it, it - 1)
            }
        }
        return getDataList()
    }

    private fun getDataList(): List<DataItem> {
        val list = mutableListOf<DataItem>()
        when (mInitState) {
            INIT_LOADING -> list.add(mInitLoadingData)
            INIT_FAILURE -> list.add(mInitFailureData)
            INIT_END -> {
                mData.size.also {
                    if (it == 0) {
                        if (enableEmpty) list.add(mEmptyData)
                    } else {
                        list.addAll(mData)
                        if (enableMore) {
                            when (mMoreState) {
                                MORE_LOADING -> list.add(mMoreLoadingData)
                                MORE_FAILURE -> list.add(mMoreFailureData)
                                MORE_END -> if (!enableMoreGone) list.add(mMoreEndData)
                            }
                        }
                    }
                }
            }
        }
        return list
    }

}