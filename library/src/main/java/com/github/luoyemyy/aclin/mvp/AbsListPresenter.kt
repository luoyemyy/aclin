package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle

abstract class AbsListPresenter(app: Application) : AbsPresenter(app) {

    val listLiveData by lazy {
        object : ListLiveData() {
            override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
                return loadListData(bundle, paging, loadType)
            }
        }
    }

    open fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
        return null
    }

    override fun loadData(bundle: Bundle?) {
        listLiveData.loadInit(bundle)
    }

}