package com.github.luoyemyy.aclin.mvp


class NotifyData<T>(@LoadParams.LoadType var loadType: Int, var items: List<DataItem<T>>)

open class DataItem<T>() {

    constructor(data: T?) : this() {
        this.data = data
    }

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

    fun areItemsTheSame(oldItem: DataItem<T>): Boolean {
        return this.type == oldItem.type && this.data == oldItem.data
    }

    fun areContentsTheSame(oldItem: DataItem<T>): Boolean {
        return !mUsePayload
    }

    fun getChangePayload(oldItem: DataItem<T>): Any? {
        return if (mUsePayload) {
            mUsePayload = false
            Any()
        } else null
    }

}

