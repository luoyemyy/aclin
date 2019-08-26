package com.github.luoyemyy.aclin.image.picker.gallery

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.github.luoyemyy.aclin.mvp.AbsAdapter
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.aclin.mvp.ListLiveData

abstract class GalleryAdapter(owner: LifecycleOwner, listLiveData: ListLiveData) : AbsAdapter(owner, listLiveData) {
    override fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {
        binding.setVariable(1, item)
        binding.executePendingBindings()
    }

    override fun enableLoadMore(): Boolean {
        return false
    }

    override fun enableEmpty(): Boolean {
        return false
    }

    override fun enableInit(): Boolean {
        return false
    }

    override fun getItemClickViews(binding: ViewDataBinding): List<View> {
        return listOf(binding.root)
    }
}