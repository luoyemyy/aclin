package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class SimpleAdapter(owner: LifecycleOwner, listLiveData: ListLiveData) :
        AbsAdapter<DataItem, ViewDataBinding>(owner, listLiveData) {

    override fun getItemClickViews(binding: ViewDataBinding): List<View> {
        return listOf(binding.root)
    }
}