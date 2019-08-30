package com.github.luoyemyy.aclin.mvp

open class DataItem(val type: Int = DataSet.CONTENT) {

    private var mUsePayload: Boolean = false

    fun hasPayload() {
        mUsePayload = true
    }

    open fun areItemsTheSame(oldItem: DataItem): Boolean {
        return this == oldItem
    }

    open fun areContentsTheSame(oldItem: DataItem): Boolean {
        return !mUsePayload
    }

    open fun getChangePayload(oldItem: DataItem): Any? {
        return if (mUsePayload) {
            mUsePayload = false
            Any()
        } else null
    }
}

data class TextItem(var text: String) : DataItem()

data class DataItemChange(var data: List<DataItem>, var changeAll: Boolean = false)