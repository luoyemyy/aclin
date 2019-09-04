package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class ReversedAdapter<T : DataItem, B : ViewDataBinding>(owner: LifecycleOwner, listLiveData: ListLiveData) :
        AbsAdapter<T, B>(owner, listLiveData) {

    override fun reversed(): Boolean {
        return true
    }

    override fun enableInit(): Boolean {
        return false
    }
}