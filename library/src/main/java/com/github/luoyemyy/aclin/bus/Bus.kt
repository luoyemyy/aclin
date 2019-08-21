package com.github.luoyemyy.aclin.bus

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread

/**
 *
 */
object Bus {

    private val mCallbacks = mutableListOf<Callback>()
    private val mHandler = Handler(Looper.getMainLooper())

    private val mDebugListeners = mutableListOf<BusDebugListener>()

    interface Callback : BusResult {
        fun interceptEvent(): String
    }

    /**
     * 注册观察者，如果当前callback已经被注册过，则忽略
     */
    @MainThread
    fun register(callback: Callback) {
        if (mCallbacks.none { it == callback }) {
            mCallbacks.add(callback)
            //debug
            debugOnRegister(callback)
        }
    }

    /**
     * 一个事件只能有一个观察者
     * 注册观察者,如果此事件有观察者，先移除已存在的，然后设置新的
     */
    @MainThread
    fun replaceRegister(callback: Callback) {
        mCallbacks.removeAll { it.interceptEvent() == callback.interceptEvent() }
        mCallbacks.add(callback)
        //debug
        debugOnRegister(callback)
    }

    /**
     * 注销观察者
     */
    @MainThread
    fun unRegister(callback: Callback) {
        mCallbacks.remove(callback)
        //debug
        debugOnUnRegister(callback)
    }

    /**
     * 派发消息
     */
    fun post(
        event: String,
        intValue: Int = 0,
        longValue: Long = 0L,
        boolValue: Boolean = false,
        stringValue: String? = null,
        extra: Bundle? = null
    ) {
        mHandler.post {
            BusMsg(event, intValue, longValue, boolValue, stringValue, extra).apply {
                mCallbacks.filter { it.interceptEvent() == event }
                    .apply { debugOnPost(event, this) }
                    .forEach { it.busResult(this) }
            }

        }
    }

    fun addDebug(debugListener: BusDebugListener) {
        mDebugListeners.add(debugListener)
    }

    //debug
    private fun debugOnRegister(callback: Callback) {
        mDebugListeners.forEach {
            it.onRegister(callback, mCallbacks)
        }
    }

    //debug
    private fun debugOnUnRegister(callback: Callback) {
        mDebugListeners.forEach {
            it.onUnRegister(callback, mCallbacks)
        }
    }

    //debug
    private fun debugOnPost(event: String, callbacks: List<Callback>) {
        mDebugListeners.forEach {
            it.onPost(event, callbacks)
        }
    }
}
