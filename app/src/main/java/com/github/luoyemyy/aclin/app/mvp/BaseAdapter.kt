package com.github.luoyemyy.aclin.app.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.github.luoyemyy.aclin.mvp.AbsAdapter
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.aclin.mvp.ListLiveData

abstract class BaseAdapter(owner: LifecycleOwner, listLiveData: ListLiveData) : AbsAdapter(owner, listLiveData) {

    override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
        binding.setVariable(1, item)
        binding.executePendingBindings()
    }

}