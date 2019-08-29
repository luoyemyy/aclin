package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class SimpleAdapter(owner: LifecycleOwner, listLiveData: ListLiveData) :
        AbsAdapter<DataItem, ViewDataBinding>(owner, listLiveData) {

    override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
        binding.setVariable(1, item)
        binding.executePendingBindings()
    }

    override fun getItemClickViews(binding: ViewDataBinding): List<View> {
        return listOf(binding.root)
    }
}