@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package com.github.luoyemyy.aclin.mvp

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.databinding.*
import com.github.luoyemyy.aclin.ext.runDelay

abstract class AbsAdapter<T : DataItem, B : ViewDataBinding>(owner: LifecycleOwner, private val mLiveData: ListLiveData) :
        ListAdapter<DataItem, VH<ViewDataBinding>>(getDiffCallback()), AdapterExt<T, B> {

    private var mEnableSort = false
    private val mItemTouchHelper by lazy { ItemTouchHelper(SortCallback(mLiveData)) }
    private var mRecyclerView: RecyclerView? = null

    init {
        mLiveData.apply {
            configDataSet(enableEmpty(), enableLoadMore(), enableInit(), enableMoreGone())
            observeRefresh(owner, Observer { setRefreshState(it) })
            observeChange(owner, Observer {
                if (it.changeAll) {
                    submitList(null)
                }
                submitList(it.data) {
                    if (it.changeAll) {
                        it.changeAll = false
                        if (it.data.isNotEmpty()) {
                            mRecyclerView?.scrollToPosition(0)
                        }
                    }
                }
            })
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = null
    }

    fun enableSort(recyclerView: RecyclerView) {
        mEnableSort = true
        mItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun getContentItem(position: Int): T? {
        return getItem(position) as? T
    }

    fun getExtraItem(position: Int): DataItem {
        return getItem(position)
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type.let {
            if (it > 0) {
                getContentType(position)
            } else {
                it
            }
        }
    }

    override fun onBindViewHolder(holder: VH<ViewDataBinding>, position: Int) {
        triggerLoadMore(position)
        val viewType = getItemViewType(position)
        if (viewType < 0) {
            bindExtra(holder.binding, getExtraItem(position), viewType, position)
        } else {
            (holder.binding as? B)?.also { binding ->
                getContentItem(position)?.also { item ->
                    bindContent(binding, item, viewType, position)
                }
            }
        }
    }

    private fun triggerLoadMore(position: Int) {
        if (position + 1 == itemCount) {
            runDelay(300) {
                mLiveData.loadMore()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ViewDataBinding> {
        val inflater = LayoutInflater.from(parent.context)
        return (if (viewType < 0) {
            createExtraBinding(inflater, parent, viewType)?.let { binding ->
                VH(binding)
            }
        } else {
            createContentBinding(inflater, parent, viewType)?.let { binding ->
                VH(binding as ViewDataBinding).apply {
                    (binding as? B)?.also {
                        bindContentEvents(binding, this)
                    }
                }
            }
        }) ?: VH(AclinListNoneBinding.inflate(inflater, parent, false) as ViewDataBinding)
    }

    open fun createContentBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): B? {
        return getContentLayoutId(viewType).let {
            if (it > 0) {
                DataBindingUtil.inflate(inflater, it, parent, false)
            } else {
                null
            }
        }
    }

    private fun bindContentEvents(binding: B, vh: VH<*>) {
        bindItemEvents(binding, vh)
        //clicks
        getItemClickViews(binding).forEach { v ->
            v.setOnClickListener {
                onItemViewClick(binding, vh, it)
            }
        }
        //sort
        if (mEnableSort) {
            getItemSortView(binding)?.setOnTouchListener(View.OnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    mItemTouchHelper.startDrag(vh)
                }
                return@OnTouchListener false
            })
        }
    }

    open fun createExtraBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
        return getExtraLayoutId(viewType).let {
            if (it > 0) {
                DataBindingUtil.inflate(inflater, it, parent, false)
            } else {
                createExtraDefaultBinding(inflater, parent, viewType)
            }
        }
    }

    private fun createExtraDefaultBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
        return when (viewType) {
            DataSet.INIT_LOADING -> AclinListInitLoadingBinding.inflate(inflater, parent, false)
            DataSet.INIT_FAILURE -> AclinListInitFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mLiveData.loadRefresh() }
            }
            DataSet.EMPTY -> AclinListEmptyBinding.inflate(inflater, parent, false)
            DataSet.MORE_LOADING -> AclinListMoreLoadingBinding.inflate(inflater, parent, false)
            DataSet.MORE_FAILURE -> AclinListMoreFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mLiveData.loadMore() }
            }
            DataSet.MORE_END -> AclinListMoreEndBinding.inflate(inflater, parent, false)
            else -> null
        }
    }
}