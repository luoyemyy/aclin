package com.github.luoyemyy.aclin.ext

import android.os.Handler

fun runDelay(delay: Long, runnable: () -> Unit) {
    Handler().postDelayed(runnable, delay)
}