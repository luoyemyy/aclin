package com.github.luoyemyy.aclin.mvp

class LoadType {

    private var loadType = 0

    fun isInit() = loadType == 1
    fun isRefresh() = loadType == 2
    fun isMore() = loadType == 3
    fun isSearch() = loadType == 4

    fun init(): Boolean {
        return if (loadType == 0) {
            loadType = 1
            true
        } else false
    }

    fun refresh(): Boolean {
        return if (loadType == 0) {
            loadType = 2
            true
        } else false
    }

    fun more(): Boolean {
        return if (loadType == 0) {
            loadType = 3
            true
        } else false
    }

    fun search(): Boolean {
        return if (loadType == 0) {
            loadType = 4
            true
        } else false
    }

    fun complete() {
        loadType = 0
    }
}