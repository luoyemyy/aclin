package com.github.luoyemyy.aclin.mvp

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.databinding.*
import com.github.luoyemyy.aclin.ext.runDelay

abstract class AbsAdapter(owner: LifecycleOwner, private val mListLiveData: ListLiveData,
    diffCallback: DiffUtil.ItemCallback<DataItem> = object : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return mListLiveData.areContentsTheSame(oldItem, newItem)
        }
    }) : ListAdapter<DataItem, VH<ViewDataBinding>>(diffCallback), AdapterExt {


    private var mRecyclerView: RecyclerView? = null
    private var mEnableSort = false
    private val mItemTouchHelper by lazy {
        ItemTouchHelper(SortCallback(mListLiveData, this))
    }

    init {
        mListLiveData.apply {
            configDataSet(enableEmpty(), enableLoadMore(), enableInit(), enableMoreGone())
            removeObservers(owner)
            refreshLiveData.removeObservers(owner)
            changeLiveData.removeObservers(owner)
            observe(owner, Observer {
                if (it.changeAll) {
                    submitList(null) {
                        mRecyclerView?.scrollToPosition(0)
                    }
                }
                submitList(it.data)
            })
            refreshLiveData.observe(owner, Observer {
                setRefreshState(it)
            })
            changeLiveData.observe(owner, Observer {
                if (it.getBoolean("payload")) {
                    notifyItemChanged(it.getInt("position"), it)
                } else {
                    notifyItemChanged(it.getInt("position"))
                }
            })
        }
    }

    fun enableSort(recyclerView: RecyclerView) {
        mEnableSort = true
        mItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = null
    }

    public override fun getItem(position: Int): DataItem {
        return super.getItem(position)
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
            bindExtra(holder.binding, getItem(position), viewType, position)
        } else {
            bindContent(holder.binding, getItem(position), viewType, position)
        }
    }


    override fun onBindViewHolder(holder: VH<ViewDataBinding>, position: Int, payloads: MutableList<Any>) {
        if (payloads.size == 0) {
            onBindViewHolder(holder, position)
        } else {
            triggerLoadMore(position)
            val viewType = getItemViewType(position)
            if (viewType < 0) {
                bindExtra(holder.binding, getItem(position), viewType, holder.adapterPosition, payloads)
            } else {
                bindContent(holder.binding, getItem(position), viewType, holder.adapterPosition, payloads)
            }
        }
    }

    private fun triggerLoadMore(position: Int) {
        if (position + 1 == itemCount) {
            runDelay(300) {
                mListLiveData.loadMore()
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
                VH(binding).apply {
                    bindContentEvents(this)
                }
            }
        }) ?: VH(AclinNoneBinding.inflate(inflater, parent, false) as ViewDataBinding)
    }

    private fun bindContentEvents(vh: VH<ViewDataBinding>) {
        bindItemEvents(vh)
        //clicks
        getItemClickViews(vh.binding).forEach { v ->
            v.setOnClickListener {
                onItemViewClick(vh, it)
            }
        }
        //sort
        if (mEnableSort) {
            getItemSortView(vh.binding)?.setOnTouchListener(View.OnTouchListener { _, event ->
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
            DataSet.INIT_LOADING -> AclinInitLoadingBinding.inflate(inflater, parent, false)
            DataSet.INIT_FAILURE -> AclinInitFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mListLiveData.loadRefresh() }
            }
            DataSet.EMPTY -> AclinEmptyBinding.inflate(inflater, parent, false)
            DataSet.MORE_LOADING -> AclinMoreLoadingBinding.inflate(inflater, parent, false)
            DataSet.MORE_FAILURE -> AclinMoreFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mListLiveData.loadMore() }
            }
            DataSet.MORE_END -> AclinMoreEndBinding.inflate(inflater, parent, false)
            else -> null
        }
    }

    open fun createContentBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
        return getContentLayoutId(viewType).let {
            if (it > 0) {
                DataBindingUtil.inflate(inflater, it, parent, false)
            } else {
                null
            }
        }
    }
}