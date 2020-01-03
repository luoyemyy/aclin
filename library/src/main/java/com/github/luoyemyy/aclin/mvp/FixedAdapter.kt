package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class FixedAdapter<T, BIND : ViewDataBinding>(owner: LifecycleOwner, liveData: ListLiveData<T>) : MvpAdapter<T, BIND>(owner, liveData) {
    init {
        enableMore = false
    }
}