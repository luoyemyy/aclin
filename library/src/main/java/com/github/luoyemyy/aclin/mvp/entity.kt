package com.github.luoyemyy.aclin.mvp

import android.os.Bundle

open class DataItem(val type: Int = DataSet.CONTENT)

data class DataItemChange(val position: Int, var payload: Boolean = false, var data: Bundle? = null)

data class DataItemGroup(val changeAll: Boolean, val data: List<DataItem>?)