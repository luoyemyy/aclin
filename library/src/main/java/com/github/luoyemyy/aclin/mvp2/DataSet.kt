package com.github.luoyemyy.aclin.mvp2

class DataSet<T> {

    private val dataList: MutableList<DataItem<T>> = mutableListOf()

    init {
        dataList.add(DataItem(1))
    }
}