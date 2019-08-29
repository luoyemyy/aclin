package com.github.luoyemyy.aclin.app.mvp

import android.os.Bundle
import androidx.core.os.bundleOf
import com.github.luoyemyy.aclin.mvp.DataItem

data class TextItem(val key: String, var value: String = key) : DataItem() {
    fun getText(): String = "$key:$value"

    override fun getChangePayload(oldItem: DataItem): Bundle? {
        super.getChangePayload(oldItem)
        return bundleOf()
    }
}