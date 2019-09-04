package com.github.luoyemyy.aclin.mvp

import java.util.*

class DataSet(var pageSize: Int = 10,
              var enableEmptyItem: Boolean = true,
              var enableMoreItem: Boolean = true,
              var enableMoreGone: Boolean = false,
              var enableInitItem: Boolean = true,
              var reversed: Boolean = false) {

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

    /**
     * 内容列表
     */
    private val contentList: MutableList<DataItem> = mutableListOf()
    private var mInitState = INIT_LOADING
    private var mMoreState = MORE_LOADING

    fun canLoadInit() = mInitState != INIT_END

    /**
     * 判断是否可以加载更多
     */
    fun canLoadMore(): Boolean {
        return enableMoreItem && mInitState == INIT_END && mMoreState in arrayOf(MORE_LOADING, MORE_FAILURE)
    }

    private fun setLoadMoreState(list: List<DataItem>?) {
        if (enableMoreItem) {
            mMoreState = if (list.isNullOrEmpty() || list.size < pageSize) {
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
        contentList.clear()
        if (!list.isNullOrEmpty()) {
            contentList.addAll(list)
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
            if (reversed) {
                contentList.addAll(0, list)
            } else {
                contentList.addAll(list)
            }
        }
        setLoadMoreState(list)
        return getDataList()
    }

    fun move(start: DataItem?, end: DataItem?): List<DataItem>? {
        if (start == null || end == null || start == end || start.type <= 0 || end.type <= 0) {
            return null
        }
        val startPosition = contentList.indexOf(start)
        val endPosition = contentList.indexOf(end)
        if (startPosition <= -1 || endPosition <= -1) {
            return null
        }
        if (startPosition < endPosition) {
            (startPosition until endPosition).forEach {
                Collections.swap(contentList, it, it + 1)
            }
        } else if (startPosition > endPosition) {
            (startPosition downTo endPosition + 1).forEach {
                Collections.swap(contentList, it, it - 1)
            }
        }
        return getDataList()
    }


    fun getContentList(): MutableList<DataItem> {
        return contentList
    }

    fun getDataList(): List<DataItem> {
        val list = mutableListOf<DataItem>()
        when (mInitState) {
            INIT_LOADING -> if (enableInitItem) list.add(DataItem(INIT_LOADING))
            INIT_FAILURE -> if (enableInitItem) list.add(DataItem(INIT_FAILURE))
            INIT_END -> {
                contentList.size.also {
                    if (it == 0) {
                        if (enableEmptyItem) list.add(DataItem(EMPTY))
                    } else {
                        if (!reversed) {
                            list.addAll(contentList)
                        }
                        if (enableMoreItem) {
                            when (mMoreState) {
                                MORE_LOADING -> list.add(DataItem(MORE_LOADING))
                                MORE_FAILURE -> list.add(DataItem(MORE_FAILURE))
                                MORE_END -> if (!enableMoreGone) list.add(DataItem(MORE_END))
                            }
                        }
                        if (reversed) {
                            list.addAll(contentList)
                        }
                    }
                }
            }
        }
        return list
    }
}