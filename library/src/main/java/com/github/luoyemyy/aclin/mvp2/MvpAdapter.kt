package com.github.luoyemyy.aclin.mvp2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.databinding.*
import com.github.luoyemyy.aclin.mvp.VH

abstract class MvpAdapter<T, BIND : ViewDataBinding>(owner: LifecycleOwner, private val mLiveData: ListLiveData<T>) : RecyclerView.Adapter<VH<ViewDataBinding>>() {

    private var mEnableMore: Boolean = true
    private var mReversed: Boolean = false
    private val mDiffer: MvpDiffer<T> = MvpDiffer(this)
    private val mUpdateListener: MvpDiffer.UpdateListener<T> = object : MvpDiffer.UpdateListener<T> {
        override fun onCurrentListChanged(oldList: List<DataItem<T>>?, newList: List<DataItem<T>>?) {
            tryLoadMore()
        }
    }

    init {
        mDiffer.addUpdateListener(mUpdateListener)
        mLiveData.config(mEnableMore, mReversed)
        mLiveData.observe(owner, Observer { mDiffer.update(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ViewDataBinding> {
        return if (viewType < 0) {
            VH(getExtraBinding(viewType, parent))
        } else {
            VH(getContentBinding(viewType, parent))
        }
    }

    override fun onBindViewHolder(holder: VH<ViewDataBinding>, position: Int) {
        val viewType = getItemViewType(position)
        val data = getItem(position)
        tryLoadMore(position)
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

    private fun tryLoadMore() {
        if (mEnableMore) {

//            if (mReversed && position == 0) {
//                mLiveData.loadMore()
//            } else if (!mReversed && position == itemCount - 1) {
//                mLiveData.loadMore()
//            }
        }
    }

    private fun tryLoadMore(position: Int) {
        if (mEnableMore) {
            if (mReversed && position == 0) {
                mLiveData.loadMore()
            } else if (!mReversed && position == itemCount - 1) {
                mLiveData.loadMore()
            }
        }
    }

    fun addUpdateListener(listener: MvpDiffer.UpdateListener<T>) {
        mDiffer.addUpdateListener(listener)
    }

    /**
     * 绑定列表数据
     */
    abstract fun bindContentViewHolder(binding: BIND, data: T?, viewType: Int, position: Int)

    /**
     * 获得列表模板
     */
    abstract fun getContentBinding(viewType: Int, parent: ViewGroup): BIND

    /**
     * 绑定额外数据
     */
    open fun bindExtraViewHolder(binding: ViewDataBinding, viewType: Int, position: Int) {}

    /**
     * 获得额外模板
     */
    open fun getExtraBinding(viewType: Int, parent: ViewGroup): ViewDataBinding {
        return createExtraDefaultBinding(LayoutInflater.from(parent.context), parent, viewType)
    }

    private fun createExtraDefaultBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return when (viewType) {
            DataSet.INIT_LOADING -> AclinListInitLoadingBinding.inflate(inflater, parent, false)
            DataSet.INIT_EMPTY -> AclinListEmptyBinding.inflate(inflater, parent, false)
            DataSet.INIT_FAILURE -> AclinListInitFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mLiveData.loadStart() }
            }
            DataSet.MORE_LOADING -> AclinListMoreLoadingBinding.inflate(inflater, parent, false)
            DataSet.MORE_FAILURE -> AclinListMoreFailureBinding.inflate(inflater, parent, false).apply {
                root.setOnClickListener { mLiveData.loadMore() }
            }
            DataSet.MORE_END -> AclinListMoreEndBinding.inflate(inflater, parent, false)
            else -> AclinListNoneBinding.inflate(inflater, parent, false)
        }
    }
}