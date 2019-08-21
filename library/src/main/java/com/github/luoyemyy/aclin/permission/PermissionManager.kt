package com.github.luoyemyy.aclin.permission

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Size
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.mvp.getPresenter

typealias PermissionCallback = (Array<String>) -> Unit

object PermissionManager {

    fun toSetting(fragment: Fragment, msg: String) {
        AlertDialog.Builder(fragment.requireContext()).setCancelable(false)
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok) { _, i ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", fragment.requireContext().packageName, null))
                fragment.startActivityForResult(intent, 1)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun toSetting(activity: FragmentActivity, msg: String) {
        AlertDialog.Builder(activity).setCancelable(false)
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok) { _, i ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", activity.packageName, null))
                activity.startActivityForResult(intent, 1)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 过滤出还未授权的权限
     */
    fun filterNotGrantedPermissions(context: Context, @Size(min = 1) vararg perms: String): Array<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return arrayOf()
        }
        return perms.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    class Builder private constructor() {

        /**
         * 授权成功回调
         */
        private var mGranted: PermissionCallback? = null
        /**
         * 授权拒绝回调
         */
        private var mDenied: PermissionCallback? = null

        private lateinit var mContext: Context
        private lateinit var mOwner: LifecycleOwner
        private lateinit var mAct: FragmentActivity

        constructor(fragment: Fragment) : this() {
            mContext = fragment.requireContext()
            mOwner = fragment
            mAct = fragment.requireActivity()
        }

        constructor(activity: FragmentActivity) : this() {
            mContext = activity
            mOwner = activity
            mAct = activity
        }

        /**
         * 设置授权成功回调
         */
        fun granted(callback: PermissionCallback): Builder {
            mGranted = callback
            return this
        }

        /**
         * 设置授权拒绝回调
         */
        fun denied(callback: PermissionCallback): Builder {
            mDenied = callback
            return this
        }

        fun buildAndRequest(vararg perms: String) {
            val perms2 = perms.toList().toTypedArray()
            val notGrantedPerms = filterNotGrantedPermissions(mContext, *perms)
            if (notGrantedPerms.isNullOrEmpty()) {
                mGranted?.invoke(perms2)
            } else {
                PermissionFragment.injectIfNeededIn(mAct)
                mAct.getPresenter<PermissionPresenter>().also {
                    it.callback(mOwner, mGranted ?: {}, mDenied ?: {})
                    it.request(perms2, notGrantedPerms)
                }
            }
        }
    }
}

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

