package com.github.luoyemyy.aclin.mvp

import android.os.Handler
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

abstract class AbsListAdapter(
    owner: LifecycleOwner,
    val mPresenter: AbsListPresenter,
    diffCallback: DiffUtil.ItemCallback<DataItem> = object : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return mPresenter.areContentsTheSame(oldItem, newItem)
        }
    }
) : ListAdapter<DataItem, VH<ViewDataBinding>>(diffCallback), AbsListAdapterSupport {


    private var mEnableSort = false
    private val mItemTouchHelper by lazy {
        ItemTouchHelper(SortCallback(mPresenter, this))
    }

    init {
        mPresenter.apply {
            configDataSet(enableEmpty(), enableLoadMore(), enableMoreGone())
            refreshState.observe(owner, Observer {
                setRefreshState(it)
            })
            itemList.observe(owner, Observer {
                submitList(it)
            })
            changePosition.observe(owner, Observer {
                notifyItemChanged(it)
            })
        }
    }

    fun enableSort(recyclerView: RecyclerView) {
        mEnableSort = true
        mItemTouchHelper.attachToRecyclerView(recyclerView)
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

    private fun triggerLoadMore(position: Int) {
        if (position + 1 == itemCount) {
            Handler().postDelayed({
                mPresenter.loadMore()
            }, 300)
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

    private fun createExtraBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
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
                root.setOnClickListener { mPresenter.loadRefresh() }
            }
            DataSet.EMPTY -> AclinEmptyBinding.inflate(inflater, parent, false)
            DataSet.MORE_LOADING -> AclinMoreLoadingBinding.inflate(inflater, parent, false)
            DataSet.MORE_FAILURE -> AclinMoreFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mPresenter.loadMore() }
            }
            DataSet.MORE_END -> AclinMoreEndBinding.inflate(inflater, parent, false)
            else -> null
        }
    }

    private fun createContentBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
        return getContentLayoutId(viewType).let {
            if (it > 0) {
                DataBindingUtil.inflate(inflater, it, parent, false)
            } else {
                null
            }
        }
    }


}