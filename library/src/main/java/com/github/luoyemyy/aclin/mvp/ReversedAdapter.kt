package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class ReversedAdapter<T, BIND : ViewDataBinding>(owner: LifecycleOwner, liveData: ListLiveData<T>) : MvpAdapter<T, BIND>(owner, liveData) {
    init {
        reversed = true
    }
}