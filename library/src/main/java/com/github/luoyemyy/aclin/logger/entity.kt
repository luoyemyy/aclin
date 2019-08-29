package com.github.luoyemyy.aclin.logger

import android.os.Bundle
import androidx.core.os.bundleOf
import com.github.luoyemyy.aclin.mvp.DataItem

data class LoggerItem(val text: String, val path: String, var select: Boolean = false) : DataItem() {

    override fun getChangePayload(oldItem: DataItem): Bundle? {
        super.getChangePayload(oldItem)
        return bundleOf()
    }
}