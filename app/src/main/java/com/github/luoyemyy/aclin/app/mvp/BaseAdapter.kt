package com.github.luoyemyy.aclin.app.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.github.luoyemyy.aclin.mvp.AbsListAdapter
import com.github.luoyemyy.aclin.mvp.AbsListPresenter
import com.github.luoyemyy.aclin.mvp.DataItem

abstract class BaseAdapter(owner: LifecycleOwner, presenter: AbsListPresenter) : AbsListAdapter(owner, presenter) {

    override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
        binding.setVariable(1, item)
        binding.executePendingBindings()
    }

}