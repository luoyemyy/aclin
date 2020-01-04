package com.github.luoyemyy.aclin.mvp.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.mvp.core.MvpData

abstract class FixedAdapter<T : MvpData, BIND : ViewDataBinding> : MvpAdapter<T, BIND>() {
    init {
        enableMore = false
    }

    override fun getItemClickViews(binding: BIND): List<View> {
        return listOf(binding.root)
    }
}