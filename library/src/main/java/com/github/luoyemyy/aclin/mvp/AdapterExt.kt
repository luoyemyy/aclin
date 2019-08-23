package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding

interface AdapterExt {

    /**
     * 设置刷新控件样式
     */
    fun setRefreshState(refreshing: Boolean) {}

    /**
     * 创建内容view
     */
    @LayoutRes
    fun getContentLayoutId(viewType: Int): Int

    /**
     * 额外的item
     */
    @LayoutRes
    fun getExtraLayoutId(viewType: Int): Int = 0

    /**
     *
     */
    fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int)

    fun bindContent(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int, payloads: MutableList<Any>) {
    }

    /**
     *
     */
    fun bindExtra(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {}

    fun bindExtra(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int, payloads: MutableList<Any>) {}

    /**
     * 内容类型
     */
    fun getContentType(position: Int): Int = DataSet.CONTENT

    /**
     * 获得需要绑定点击事件的view
     */
    fun getItemClickViews(binding: ViewDataBinding): List<View> = listOf()


    fun getItemSortView(binding: ViewDataBinding): View? = null

    /**
     * 点击事件处理
     */
    fun onItemViewClick(vh: VH<ViewDataBinding>, view: View) {}

    /**
     * 绑定其他事件
     */
    fun bindItemEvents(vh: VH<ViewDataBinding>) {}

    /**
     * 是否需要加载更多样式
     */
    fun enableLoadMore(): Boolean = true

    /**
     * 是否需要空数据样式
     */
    fun enableEmpty(): Boolean = true

    /**
     * 是否需要初始化数据样式
     */
    fun enableInit(): Boolean = true

    /**
     * 加载完全部数据后，是否隐藏该项目
     */
    fun enableMoreGone(): Boolean = false


}