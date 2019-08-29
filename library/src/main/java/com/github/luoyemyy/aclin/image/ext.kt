package com.github.luoyemyy.aclin.image

import android.content.Context

fun calculateImageItemSize(context: Context): Pair<Int, Int> {
    val suggestSize = context.resources.displayMetrics.density * 80
    val screenWidth = context.resources.displayMetrics.widthPixels

    val span = (screenWidth / suggestSize).toInt()
    val size = screenWidth / span
    return Pair(span, size)
}