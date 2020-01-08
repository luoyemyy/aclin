package com.github.luoyemyy.aclin.mvp.adapter

import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.mvp.core.MvpData

abstract class ReversedAdapter<T : MvpData, BIND : ViewDataBinding> : MvpAdapter<T, BIND>() {
    init {
        reversed = true
    }
}