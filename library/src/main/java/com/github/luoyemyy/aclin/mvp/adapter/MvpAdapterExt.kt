@file:Suppress("unused")

package com.github.luoyemyy.aclin.mvp.adapter

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.mvp.core.VH

interface MvpAdapterExt<T, BIND : ViewDataBinding> {

    fun notifyAfter(type: Int) {}
    /**
     * 绑定列表数据
     */
    fun bindContentViewHolder(binding: BIND, data: T?, viewType: Int, position: Int)

    /**
     * 获得列表模板
     */
    fun getContentBinding(viewType: Int, parent: ViewGroup): BIND

    /**
     * 绑定额外数据
     */
    fun bindExtraViewHolder(binding: ViewDataBinding, viewType: Int, position: Int) {}

    /**
     * 获得额外模板
     */
    fun getExtraBinding(viewType: Int, parent: ViewGroup): ViewDataBinding

    /**
     * 获得需要绑定点击事件的view
     */
    fun getItemClickViews(binding: BIND): List<View> = listOf()

    /**
     * 获得点击拖动的view
     */
    fun getItemSortView(binding: BIND): View? = null

    /**
     * 点击事件处理
     */
    fun onItemViewClick(binding: BIND, vh: VH<*>, view: View) {}

    /**
     * 绑定其他事件
     */
    fun bindItemEvents(binding: BIND, vh: VH<*>) {}
}