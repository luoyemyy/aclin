package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel

abstract class AbsListPresenter(app: Application) : AndroidViewModel(app) {

    val listLiveData by lazy {
        object : ListLiveData() {
            override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
                return this@AbsListPresenter.loadData(bundle, paging, loadType)
            }

            override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType, loadDataAfter: LoadDataAfter<DataItem>): Boolean {
                return this@AbsListPresenter.loadData(bundle, paging, loadType, loadDataAfter)
            }
        }
    }

    open fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType, loadDataAfter: LoadDataAfter<DataItem>): Boolean = false

    open fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
        return null
    }

}