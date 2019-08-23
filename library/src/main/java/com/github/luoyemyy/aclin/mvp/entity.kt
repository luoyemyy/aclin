package com.github.luoyemyy.aclin.mvp

open class DataItem(val type: Int = DataSet.CONTENT)

data class DataItemGroup(val changeAll: Boolean, val data: List<DataItem>?)