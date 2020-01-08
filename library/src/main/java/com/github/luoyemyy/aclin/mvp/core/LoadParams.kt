package com.github.luoyemyy.aclin.mvp.core

import androidx.annotation.IntDef

class LoadParams {

    companion object {
        const val TYPE_INIT = 0
        const val TYPE_UPDATE = 1
        const val TYPE_START = 2
        const val TYPE_REFRESH = 3
        const val TYPE_MORE = 4

        fun isRefresh(type: Int): Boolean = type == TYPE_REFRESH
        fun isStart(type: Int): Boolean = type == TYPE_START
        fun isMore(type: Int): Boolean = type == TYPE_MORE
    }

    @IntDef(TYPE_INIT, TYPE_UPDATE, TYPE_START, TYPE_REFRESH, TYPE_MORE)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LoadType

    var page: Long = 1
    var prevPage: Long = page
    var loadType: Int = TYPE_UPDATE

    fun isStart(): Boolean = loadType == TYPE_START
    fun isRefresh(): Boolean = loadType == TYPE_REFRESH
    fun isMore(): Boolean = loadType == TYPE_MORE

    fun start() {
        loadType = TYPE_START
    }

    fun refresh() {
        loadType = TYPE_REFRESH
    }

    fun more() {
        loadType = TYPE_MORE
    }

    fun nextPage() {
        prevPage = page
        page++
    }

    fun resetPage() {
        prevPage = page
        page = 1
    }

    fun backPage() {
        page = prevPage
    }

}