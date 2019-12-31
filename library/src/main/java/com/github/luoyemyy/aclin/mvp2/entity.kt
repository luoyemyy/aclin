package com.github.luoyemyy.aclin.mvp2


class DataItem<T>() {

    var type: Int = 0
    var data: T? = null
    private var mUsePayload: Boolean = false

    constructor(type: Int) : this() {
        this.type = type
    }

    constructor(data: T, mapType: (T) -> Int) : this() {
        this.type = mapType(data)
        this.data = data
    }

    fun hasPayload() {
        mUsePayload = true
    }

    fun areItemsTheSame(oldData: T): Boolean {
        return this.data == oldData
    }

    fun areContentsTheSame(oldData: T): Boolean {
        return !mUsePayload
    }

    fun getChangePayload(oldData: T): Any? {
        return if (mUsePayload) {
            mUsePayload = false
            Any()
        } else null
    }



}

