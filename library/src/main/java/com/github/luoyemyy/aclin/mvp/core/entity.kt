@file:Suppress("UNUSED_PARAMETER")

package com.github.luoyemyy.aclin.mvp.core


class NotifyItems<T : MvpData>(@LoadParams.LoadType var loadType: Int, var items: List<DataItem<T>>)

class TextData(var text: String?) : MvpData()

open class MvpData {
    @Transient
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
        return type >= 0 && this == oldItem
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

