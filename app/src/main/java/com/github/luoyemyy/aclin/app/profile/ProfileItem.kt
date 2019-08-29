package com.github.luoyemyy.aclin.app.profile

import android.os.Bundle
import androidx.core.os.bundleOf
import com.github.luoyemyy.aclin.mvp.DataItem

data class ProfileItem(var desc: String, var active: Boolean) : DataItem() {

    override fun getChangePayload(oldItem: DataItem): Bundle? {
        super.getChangePayload(oldItem)
        return bundleOf()
    }
}