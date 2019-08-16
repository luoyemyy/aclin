package com.github.luoyemyy.aclin.mvp

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class AbsPresenter(var mApp: Application) : AndroidViewModel(mApp) {


}