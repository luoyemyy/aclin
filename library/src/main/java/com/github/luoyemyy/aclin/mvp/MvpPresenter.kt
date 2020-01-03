package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import java.util.concurrent.atomic.AtomicBoolean

abstract class MvpPresenter(app: Application) : AndroidViewModel(app) {

    private val mInit = AtomicBoolean(false)

    fun loadInit(bundle: Bundle?) {
        if (mInit.compareAndSet(false, true)) {
            loadData(bundle)
        }
    }

    protected abstract fun loadData(bundle: Bundle?)

}