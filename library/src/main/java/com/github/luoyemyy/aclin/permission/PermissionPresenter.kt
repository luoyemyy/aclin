package com.github.luoyemyy.aclin.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class PermissionPresenter(app: Application) : AndroidViewModel(app) {

    private val mGranted = MutableLiveData<Array<String>>()
    private val mDenied = MutableLiveData<Array<String>>()
    private lateinit var mAllPerms: Array<String>

    internal val request = MutableLiveData<Array<String>>()

    /**
     * @param owner 发起授权的页面
     */
    internal fun callback(owner: LifecycleOwner, granted: PermissionCallback, denied: PermissionCallback) {
        //重复点击授权时，首先清除可能在上一次注册过的
        mGranted.removeObservers(owner)
        mDenied.removeObservers(owner)
        mGranted.observe(owner, Observer {
            if (!it.isNullOrEmpty()) {
                clear()
                granted(it)
            }
        })
        mDenied.observe(owner, Observer {
            if (!it.isNullOrEmpty()) {
                clear()
                denied(it)
            }
        })
    }

    internal fun granted() {
        mGranted.postValue(mAllPerms)
    }

    internal fun denied(perms: Array<String>) {
        mDenied.postValue(perms)
    }

    internal fun request(allPerms: Array<String>, notGrantedPerms: Array<String>) {
        mAllPerms = allPerms
        request.postValue(notGrantedPerms)
    }

    /**
     * 清除liveData中的最后一个值，防止下次 observe 时，会发送最后一个值
     */
    private fun clear() {
        mDenied.postValue(null)
        mGranted.postValue(null)
        request.postValue(null)
    }
}