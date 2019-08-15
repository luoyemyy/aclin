package com.github.luoyemyy.aclin.mvp

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * example:
 *
 *  mStickView的高度需要大于或等于置顶类型item的高度
 *
 *  <android.support.constraint.ConstraintLayout
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent"
 *      app:layout_behavior="@string/appbar_scrolling_view_behavior">

 *      <android.support.v4.widget.SwipeRefreshLayout
 *          android:id="@+id/swipeRefreshLayout"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent">

 *          <android.support.v7.widget.RecyclerView
 *              android:id="@+id/recyclerView"
 *              android:layout_width="match_parent"
 *              android:layout_height="match_parent" />
 *      </android.support.v4.widget.SwipeRefreshLayout>

 *      <TextView
 *          id="+id/stickView"
 *          android:layout_width="match_parent"
 *          android:layout_height="wrap_content">
 *          android:visibility="gone" />

 *  </android.support.constraint.ConstraintLayout>
 *
 *  StickHelper.attachToRecyclerView(mRecyclerView, mStickView, object : StickHelper.Callback {
 *      override fun isStick(position: Int): Boolean {
 *          return getItem(position)?.isStickType?:false
 *      }
 *      override fun setStickViewContent(position: Int) {
 *          mStickView.text = getItem(position)?.name
 *      }
 *  })
 *
 */
class StickHelper private constructor(private val mStickView: View, private val mCallback: Callback) : RecyclerView.OnScrollListener() {

    companion object {
        fun attachToRecyclerView(recyclerView: RecyclerView, stickView: View, callback: Callback) {
            stickView.visibility = View.INVISIBLE
            recyclerView.addOnScrollListener(StickHelper(stickView, callback))
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        onScrolled(recyclerView, 0, 0)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val count = recyclerView.childCount
        if (count <= 0) {
            mStickView.visibility = View.GONE
            return
        }
        //取到当前显示的第一个item
        val firstVisibleView = recyclerView.getChildAt(0) ?: let {
            mStickView.visibility = View.GONE
            return
        }
        //第一个显示的item在所有数据中的位置
        val position = recyclerView.getChildAdapterPosition(firstVisibleView)
        if (position < 0) {
            mStickView.visibility = View.GONE
            return
        }
        //判断这个item是不是置顶的类型
        if (mCallback.isStick(position)) {
            //如果是置顶类型
            if (position == 0 && firstVisibleView.top >= 0) {
                //如果当前是所有数据中的第一个，并且这个item是全部显示的，则隐藏stickView
                mStickView.visibility = View.GONE
            } else {
                //否则将stickView 全部显示出来，重置偏移量，设置stickView的显示内容
                mStickView.visibility = View.VISIBLE
                mStickView.translationY = 0f
                mCallback.setStickViewContent(position)
            }
        } else {
            //不是置顶类型，则查询到上一个置顶类型的位置
            val prevPosition = (position downTo 0).firstOrNull { mCallback.isStick(it) }
            if (prevPosition != null) {
                //如果有上一个置顶类型，则显示stickView ，并设置内容
                mStickView.visibility = View.VISIBLE
                mCallback.setStickViewContent(prevPosition)
                //接下来判断是否需要偏移stickView
                if (count > 1) {
                    //查询下一个置顶类型的位置
                    val nextPosition = (1 until count).firstOrNull {
                        recyclerView.getChildAt(it)?.let { v ->
                            mCallback.isStick(recyclerView.getChildAdapterPosition(v))
                        } ?: false
                    }
                    if (nextPosition != null) {
                        //如果有，则计算下一个置顶类型的位置到顶部的距离，如果小于stickView的高度，则将stickView向上偏移高度差，否则重置偏移量
                        val nextView = recyclerView.getChildAt(nextPosition)
                        if (nextView.top < mStickView.height) {
                            mStickView.translationY = (nextView.top - mStickView.height).toFloat()
                        } else {
                            mStickView.translationY = 0f
                        }
                    } else {
                        mStickView.translationY = 0f
                    }
                }
            } else {
                //没有上一个置顶类型，隐藏stickView
                mStickView.visibility = View.GONE
            }
        }
    }

    interface Callback {
        fun isStick(position: Int): Boolean
        fun setStickViewContent(position: Int)
    }
}