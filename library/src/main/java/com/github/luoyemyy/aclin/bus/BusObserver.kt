@file:Suppress("UNUSED_PARAMETER", "unused")

package com.github.luoyemyy.aclin.bus

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.github.luoyemyy.aclin.ext.runImmediate

/**
 * bus 管理注册器
 * 注册后，此事件监听会绑定生命周期，不用手动去释放
 */
internal class BusObserver constructor(
    private val lifecycle: Lifecycle,
    private val mResult: BusResult,
    private var mEvent: String
) : Bus.Callback, LifecycleObserver {

    private val pendingEvents: MutableList<BusMsg> = mutableListOf()

    init {
        lifecycle.addObserver(this)
    }

    fun register(replace: Boolean) {
        if (replace) {
            Bus.replaceRegister(this)
        } else {
            Bus.register(this)
        }
    }

    private fun isResume(): Boolean {
        return lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(source: LifecycleOwner) {
        pendingEvents.clear()
        lifecycle.removeObserver(this)
        Bus.unRegister(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(source: LifecycleOwner) {
        runImmediate {
            if (isResume()) {
                pendingEvents.forEach {
                    mResult.busResult(it)
                }
                pendingEvents.clear()
            }
        }
    }

    override fun interceptEvent(): String = mEvent

    override fun busResult(msg: BusMsg) {
        if (isResume()) {
            mResult.busResult(msg)
        } else {
            if (mEvent.endsWith("@REPLACE")) {
                //如果 event 包含替换后缀，则会删除之前相同的事件，只保留最新的一个
                pendingEvents.removeAll { it.event == mEvent }
            }
            pendingEvents.add(msg)
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return mResult.hashCode().toString()
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other?.toString()
    }
}
