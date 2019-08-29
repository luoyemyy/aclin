package com.github.luoyemyy.aclin.mvp

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel

abstract class AbsPresenter(app: Application) : AndroidViewModel(app) {

    open fun setupArgs(bundle: Bundle?) {

    }
}