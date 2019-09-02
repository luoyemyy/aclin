package com.github.luoyemyy.aclin.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class PermissionPresenter(app: Application) : AndroidViewModel(app) {

    internal var request = MutableLiveData<PermissionManager.Request>()
    internal var response = MutableLiveData<PermissionManager.Response>()

}