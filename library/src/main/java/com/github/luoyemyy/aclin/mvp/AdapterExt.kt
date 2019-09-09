package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.github.luoyemyy.aclin.BR

interface AdapterExt<T : DataItem, B : ViewDataBinding> {

    /**
     * 设置刷新控件样式
     */
    fun setRefreshState(refreshing: Boolean) {}

    /**
     * 主要的item
     */
    @LayoutRes
    fun getContentLayoutId(viewType: Int): Int

    /**
     * 额外的item
     */
    @LayoutRes
    fun getExtraLayoutId(viewType: Int): Int = 0

    /**
     * 绑定主要内容
     */
    fun bindContent(binding: B, item: T, viewType: Int, position: Int) {
        binding.setVariable(BR.entity, item)
        binding.executePendingBindings()
    }

    /**
     * 绑定额外内容
     */
    fun bindExtra(binding: ViewDataBinding, item: DataItem, viewType: Int, position: Int) {}

    /**
     * 内容类型
     */
    fun getContentType(position: Int): Int = DataSet.CONTENT

    /**
     * 获得需要绑定点击事件的view
     */
    fun getItemClickViews(binding: B): List<View> = listOf()

    /**
     * 获得点击拖动的view
     */
    fun getItemSortView(binding: B): View? = null

    /**
     * 点击事件处理
     */
    fun onItemViewClick(binding: B, vh: VH<*>, view: View) {}

    /**
     * 绑定其他事件
     */
    fun bindItemEvents(binding: B, vh: VH<*>) {}

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

    /**
     * 翻转列表
     */
    fun reversed(): Boolean = false

    /**
     * 每页加载数量
     */
    fun pageSize(): Int = 10

}