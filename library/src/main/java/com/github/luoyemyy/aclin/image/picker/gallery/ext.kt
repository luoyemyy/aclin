package com.github.luoyemyy.aclin.image.picker.gallery

import android.content.Context
import com.github.luoyemyy.aclin.ext.dp2px

typealias GalleryCallback = (ArrayList<String>) -> Unit

internal fun calculateImageItemSize(context: Context, suggestDp: Int = 80): Pair<Int, Int> {
    return context.resources.displayMetrics.widthPixels.let {
        val span = (it / context.dp2px(suggestDp))
        Pair(span, it / span)
    }
}