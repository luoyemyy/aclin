package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class FixedAdapter<T : DataItem, B : ViewDataBinding>(owner: LifecycleOwner, listLiveData: ListLiveData) : AbsAdapter<T, B>(owner, listLiveData) {

    override fun enableEmpty(): Boolean {
        return false
    }

    override fun enableInit(): Boolean {
        return false
    }

    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun getItemClickViews(binding: B): List<View> {
        return listOf(binding.root)
    }
}