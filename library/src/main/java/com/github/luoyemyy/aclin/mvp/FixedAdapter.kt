package com.github.luoyemyy.aclin.mvp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class FixedAdapter<B : ViewDataBinding>(private var mData: List<Any>?) : RecyclerView.Adapter<VH<B>>() {

    @LayoutRes
    abstract fun getContentLayoutId(viewType: Int): Int

    fun setData(data: List<Any>?) {
        mData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<B> {
        return VH(DataBindingUtil.inflate(LayoutInflater.from(parent.context), getContentLayoutId(viewType), parent, false))
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    override fun onBindViewHolder(holder: VH<B>, position: Int) {
        holder.binding.apply {
            mData?.get(position)?.apply {
                setVariable(1, this)
                executePendingBindings()
            }
        }
    }

}