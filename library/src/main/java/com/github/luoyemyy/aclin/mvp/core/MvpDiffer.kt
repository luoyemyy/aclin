package com.github.luoyemyy.aclin.mvp.core

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.mvp.ext.UpdateListener
import java.util.concurrent.CopyOnWriteArrayList

class MvpDiffer<T : MvpData>(adapter: RecyclerView.Adapter<VH<ViewDataBinding>>) {

    private val mUpdateCallback: ListUpdateCallback = AdapterListUpdateCallback(adapter)
    private val mListeners = CopyOnWriteArrayList<UpdateListener<T>>()
    private var mImmutableList: List<DataItem<T>> = listOf()
    private var mMaxScheduledGeneration: Int = 0

    fun itemType(position: Int): Int = mImmutableList[position].type

    fun countItem(): Int = mImmutableList.size

    fun getItem(position: Int): T? = mImmutableList[position].data

    fun addUpdateListener(listener: UpdateListener<T>) {
        mListeners.add(listener)
    }

    fun update(newList: List<DataItem<T>>, updateListener: UpdateListener<T>? = null) {
        val runGeneration = ++mMaxScheduledGeneration
        val oldList = mImmutableList
        if (newList.isEmpty()) {
            if (oldList.isNotEmpty()) {
                mImmutableList = listOf()
                mUpdateCallback.onRemoved(0, oldList.size)
                notifyUpdateListener(updateListener, oldList, newList)
            }
            return
        }
        if (oldList.isEmpty()) {
            mImmutableList = newList
            mUpdateCallback.onInserted(0, newList.size)
            notifyUpdateListener(updateListener, oldList, newList)
            return
        }
        runOnThread {
            val result: DiffUtil.DiffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldList.size
                override fun getNewListSize(): Int = newList.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldList[oldItemPosition].let { oldItem ->
                        newList[newItemPosition].let { newItem ->
                            newItem.areItemsTheSame(oldItem)
                        }
                    }
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldList[oldItemPosition].let { oldItem ->
                        newList[newItemPosition].let { newItem ->
                            newItem.areContentsTheSame(oldItem)
                        }
                    }
                }

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                    return oldList[oldItemPosition].let { oldItem ->
                        newList[newItemPosition].let { newItem ->
                            newItem.getChangePayload(oldItem)
                        }
                    }
                }
            })
            runOnMain {
                if (mMaxScheduledGeneration == runGeneration) {
                    mImmutableList = newList
                    result.dispatchUpdatesTo(mUpdateCallback)
                    notifyUpdateListener(updateListener, oldList, newList)
                }
            }
        }
    }

    private fun notifyUpdateListener(updateListener: UpdateListener<T>?, oldList: List<DataItem<T>>?, newList: List<DataItem<T>>?) {
        updateListener?.invoke(oldList, newList)
        mListeners.forEach { it.invoke(oldList, newList) }
    }
}