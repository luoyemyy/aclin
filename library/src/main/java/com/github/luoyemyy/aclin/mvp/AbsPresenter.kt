package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel

abstract class AbsPresenter(app: Application) : AndroidViewModel(app) {

    private var mInitialized: Boolean = false

    fun loadInit(bundle: Bundle?) {
        if (!mInitialized) {
            mInitialized = true
            loadData(bundle)
        }
    }

    protected abstract fun loadData(bundle: Bundle?)
}