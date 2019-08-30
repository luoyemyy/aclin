package com.github.luoyemyy.aclin.app.mvp

import com.github.luoyemyy.aclin.mvp.DataItem

data class TextItem(val key: String, var value: String = key) : DataItem() {
    fun getText(): String = "$key:$value"
}