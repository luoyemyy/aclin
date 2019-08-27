package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class SimpleAdapter(owner: LifecycleOwner, listLiveData: ListLiveData) : AbsAdapter(owner, listLiveData) {

    override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
        binding.setVariable(1, item)
        binding.executePendingBindings()
    }

    override fun enableEmpty(): Boolean {
        return false
    }

    override fun enableInit(): Boolean {
        return false
    }

    override fun enableLoadMore(): Boolean {
        return false
    }
}