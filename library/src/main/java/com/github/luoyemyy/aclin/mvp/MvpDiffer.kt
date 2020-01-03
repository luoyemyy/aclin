package com.github.luoyemyy.aclin.mvp

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.runOnThread
import java.util.concurrent.CopyOnWriteArrayList

class MvpDiffer<T>(adapter: RecyclerView.Adapter<VH<ViewDataBinding>>) {

    interface UpdateListener<T> {
        fun onCurrentListChanged(oldList: List<DataItem<T>>?, newList: List<DataItem<T>>?)
    }

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

    fun update(newList: List<DataItem<T>>) {
        val runGeneration = ++mMaxScheduledGeneration
        if (newList == mImmutableList) {
            return
        }
        val oldList = mImmutableList
        if (newList.isEmpty()) {
            if (oldList.isNotEmpty()) {
                mImmutableList = listOf()
                mUpdateCallback.onRemoved(0, oldList.size)
                notifyUpdateListener(oldList, newList)
            }
            return
        }
        if (oldList.isEmpty()) {
            mImmutableList = newList
            mUpdateCallback.onInserted(0, newList.size)
            notifyUpdateListener(oldList, newList)
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
                    notifyUpdateListener(oldList, newList)
                }
            }
        }
    }

    private fun notifyUpdateListener(oldList: List<DataItem<T>>?, newList: List<DataItem<T>>?) {
        mListeners.forEach { it.onCurrentListChanged(oldList, newList) }
    }
}