package com.github.luoyemyy.aclin.mvp


class NotifyData<T : MvpData>(@LoadParams.LoadType var loadType: Int, var items: List<DataItem<T>>)

class TextData(var text: String?) : MvpData()

open class MvpData {
    var dataItem: DataItem<*>? = null
}

class DataItem<T : MvpData>() {

    constructor(data: T) : this() {
        this.data = data
    }

    var type: Int = 0
    var data: T? = null
    private var usePayload: Boolean = false

    fun hasPayload() {
        usePayload = true
    }

    fun areItemsTheSame(oldItem: DataItem<T>): Boolean {
        return this == oldItem
    }

    fun areContentsTheSame(oldItem: DataItem<T>): Boolean {
        return !usePayload
    }

    fun getChangePayload(oldItem: DataItem<T>): Any? {
        return if (usePayload) {
            this.usePayload = false
            Any()
        } else null
    }

}

