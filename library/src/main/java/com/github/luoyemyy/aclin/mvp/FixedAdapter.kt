package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class FixedAdapter<T, BIND : ViewDataBinding>(owner: LifecycleOwner, liveData: ListLiveData<T>) : MvpAdapter<T, BIND>(owner, liveData) {
    init {
        enableMore = false
    }

    override fun getItemClickViews(binding: BIND): List<View> {
        return listOf(binding.root)
    }
}