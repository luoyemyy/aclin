package com.github.luoyemyy.aclin.mvp

import android.os.Bundle
import androidx.annotation.CallSuper

open class DataItem(val type: Int = DataSet.CONTENT) {

    private var mUsePayload: Boolean = false

    fun usePayload() {
        mUsePayload = true
    }

    open fun areItemsTheSame(oldItem: DataItem): Boolean {
        return this == oldItem
    }

    open fun areContentsTheSame(oldItem: DataItem): Boolean {
        return !mUsePayload
    }

    @CallSuper
    open fun getChangePayload(oldItem: DataItem): Bundle? {
        mUsePayload = false
        return null
    }
}

open class TextItem(var text: String) : DataItem()

data class DataItemChange(var data: List<DataItem>, var changeAll: Boolean = false)