package com.github.luoyemyy.aclin.mvp

import android.os.Bundle
import androidx.annotation.CallSuper

open class DataItem(val type: Int = DataSet.CONTENT) {

    private var mUsePayload: Boolean = false

    fun hasPayload() {
        mUsePayload = true
    }

    fun usePayload() {
        mUsePayload = false
    }

    open fun areItemsTheSame(oldItem: DataItem): Boolean {
        return this == oldItem
    }

    open fun areContentsTheSame(oldItem: DataItem): Boolean {
        return !mUsePayload
    }

    @CallSuper
    open fun getChangePayload(oldItem: DataItem): Bundle? {
        usePayload()
        return null
    }
}

open class TextItem(var text: String) : DataItem()

data class DataItemChange(var data: List<DataItem>, var changeAll: Boolean = false)