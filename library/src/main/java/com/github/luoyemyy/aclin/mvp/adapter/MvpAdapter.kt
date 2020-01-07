@file:Suppress("unused")

package com.github.luoyemyy.aclin.mvp.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.databinding.*
import com.github.luoyemyy.aclin.ext.TouchInfo
import com.github.luoyemyy.aclin.mvp.core.*
import com.github.luoyemyy.aclin.mvp.ext.SortCallback
import com.github.luoyemyy.aclin.mvp.ext.UpdateListener


abstract class MvpAdapter<T : MvpData, BIND : ViewDataBinding>
    : RecyclerView.Adapter<VH<ViewDataBinding>>(), MvpAdapterExt<T, BIND> {

    var enableInit: Boolean = true
    var enableMore: Boolean = true
    var reversed: Boolean = false
    var enableSort = false
    var enablePopupMenu = false
    private lateinit var mLiveData: ListLiveData<T>
    private lateinit var mDiffer: MvpDiffer<T>
    private var mRecyclerView: RecyclerView? = null
    private val mItemTouchHelper by lazy { ItemTouchHelper(SortCallback(mLiveData)) }

    fun setup(owner: LifecycleOwner, liveData: ListLiveData<T>) {
        mDiffer = MvpDiffer(this)
        mLiveData = liveData
        mLiveData.enableInit(enableInit)
        mLiveData.enableMore(enableMore)
        mLiveData.reversed(reversed)
        mLiveData.startInit()
        mLiveData.removeObservers(owner)
        mLiveData.observe(owner, Observer {
            mDiffer.update(it) { _, _ ->
                if (LoadParams.isRefresh(it.loadType) && it.items.isNotEmpty()) {
                    if (reversed) {
                        mRecyclerView?.scrollToPosition(it.items.size - 1)
                    } else {
                        mRecyclerView?.scrollToPosition(0)
                    }
                }
                notifyAfter(it.loadType)
            }
        })
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ViewDataBinding> {
        return if (viewType < 0) {
            VH(getExtraBinding(viewType, parent))
        } else {
            getContentBinding(viewType, parent).let {
                VH(it as ViewDataBinding).apply {
                    createContentEvents(it, this)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: VH<ViewDataBinding>, position: Int) {
        val viewType = getItemViewType(position)
        val data = getItem(position)
        triggerLoadMore(position)
        if (viewType < 0) {
            bindExtraViewHolder(holder.binding, viewType, position)
        } else {
            bindContentViewHolder(holder.binding as BIND, data, viewType, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mDiffer.itemType(position)
    }

    override fun getItemCount(): Int {
        return mDiffer.countItem()
    }

    protected fun getItem(position: Int): T? {
        return mDiffer.getItem(position)
    }

    private fun triggerLoadMore(position: Int) {
        (enableMore && ((reversed && position <= 1) || (!reversed && position >= itemCount - 2)))
                .apply {
                    if (this) {
                        mLiveData.loadMore()
                    }
                }
    }

    fun addUpdateListener(listener: UpdateListener<T>) {
        mDiffer.addUpdateListener(listener)
    }

    private fun createContentEvents(binding: BIND, vh: VH<*>) {
        bindItemEvents(binding, vh)
        //clicks
        getItemClickViews(binding).forEach { v ->
            v.setOnClickListener {
                onItemViewClick(binding, vh, it)
            }
        }
        //popup menu
        if (enablePopupMenu) {
            binding.root.setOnTouchListener { _, motionEvent ->
                TouchInfo.touch(motionEvent)
                false
            }
        }
        //sort
        if (enableSort) {
            getItemSortView(binding)?.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    mItemTouchHelper.startDrag(vh)
                }
                false
            }
        }
    }

    override fun getExtraBinding(viewType: Int, parent: ViewGroup): ViewDataBinding {
        return createExtraDefaultBinding(LayoutInflater.from(parent.context), parent, viewType)
    }

    private fun createExtraDefaultBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return when (viewType) {
            DataSet.INIT_LOADING -> AclinListInitLoadingBinding.inflate(inflater, parent, false)
            DataSet.INIT_EMPTY -> AclinListEmptyBinding.inflate(inflater, parent, false)
            DataSet.INIT_FAILURE -> AclinListInitFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mLiveData.loadStart(reload = true) }
            }
            DataSet.MORE_LOADING -> AclinListMoreLoadingBinding.inflate(inflater, parent, false)
            DataSet.MORE_FAILURE -> AclinListMoreFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mLiveData.loadMore(reload = true) }
            }
            DataSet.MORE_END -> AclinListMoreEndBinding.inflate(inflater, parent, false)
            else -> AclinListNoneBinding.inflate(inflater, parent, false)
        }
    }
}