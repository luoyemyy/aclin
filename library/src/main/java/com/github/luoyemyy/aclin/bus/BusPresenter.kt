package com.github.luoyemyy.aclin.bus

import android.app.Application
import androidx.lifecycle.AndroidViewModel


class BusPresenter(app: Application) : AndroidViewModel(app) {
    val busLiveData = BusLiveData()
}