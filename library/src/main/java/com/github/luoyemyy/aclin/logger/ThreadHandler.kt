package com.github.luoyemyy.aclin.logger

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

class ThreadHandler {

    private var mHandlerThread: HandlerThread = HandlerThread("ThreadHandler")
    private var mThreadHandler: Handler

    init {
        mHandlerThread.start()
        mThreadHandler = Handler(mHandlerThread.looper)
    }

    companion object {

        private val single by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ThreadHandler() }

        fun post(run: () -> Unit) {
            single.mThreadHandler.post(run)
        }

        fun getLooper(): Looper {
            return single.mHandlerThread.looper
        }
    }
}