package com.github.luoyemyy.aclin.image.picker.camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CameraPresenter(app: Application) : AndroidViewModel(app) {

    internal var request = MutableLiveData<CameraBuilder.Request>()
    internal var response = MutableLiveData<CameraBuilder.Response>()

}