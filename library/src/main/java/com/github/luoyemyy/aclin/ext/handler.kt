package com.github.luoyemyy.aclin.ext

import android.os.Handler
import androidx.arch.core.executor.ArchTaskExecutor

fun runDelay(delay: Long, runnable: () -> Unit) {
    Handler().postDelayed(runnable, delay)
}

fun runImmediate(runnable: () -> Unit) {
    Handler().post(runnable)
}

fun runOnThread(runnable: () -> Unit) {
    ArchTaskExecutor.getInstance().postToMainThread(runnable)
}