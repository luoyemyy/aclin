package com.github.luoyemyy.aclin.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Size
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.ext.confirm
import com.github.luoyemyy.aclin.mvp.ext.getPresenter

object PermissionManager {

    private var mRequestCode = 1

    fun toSetting(activity: FragmentActivity, msg: String) {
        activity.apply {
            confirm(title = getString(R.string.aclin_permission_failure_title), message = msg, okText = R.string.aclin_permission_to_setting, ok = {
                toSetting(this)
            })
        }
    }

    fun toSetting(fragment: Fragment, msg: String) {
        toSetting(fragment.requireActivity(), msg)
    }

    private fun toSetting(activity: FragmentActivity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", activity.packageName, null))
        activity.startActivityForResult(intent, 1)
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
        private var mGrantedCallback: PermissionCallback? = null
        /**
         * 授权拒绝回调
         */
        private var mDeniedCallback: PermissionCallback? = null

        private lateinit var mContext: Context
        private lateinit var mOwner: LifecycleOwner
        private lateinit var mActivity: FragmentActivity
        private var mRationale: String? = null

        constructor(fragment: Fragment, rationale: String? = null) : this() {
            mContext = fragment.requireContext()
            mOwner = fragment
            mActivity = fragment.requireActivity()
            mRationale = rationale
        }

        /**
         * 设置授权成功回调
         */
        fun granted(callback: PermissionCallback): Builder {
            mGrantedCallback = callback
            return this
        }

        /**
         * 设置授权拒绝回调
         */
        fun denied(callback: PermissionCallback): Builder {
            mDeniedCallback = callback
            return this
        }

        fun buildAndRequest(vararg perms: String) {
            val allPerms = perms.toList().toTypedArray()
            val notGrantedPerms = filterNotGrantedPermissions(mContext, *perms)
            if (notGrantedPerms.isNullOrEmpty()) {
                mGrantedCallback?.invoke(allPerms)
            } else {
                if (hasRationale() && !hasNeverAsk(notGrantedPerms)) {
                    confirmRequest(allPerms, notGrantedPerms)
                } else {
                    request(allPerms, notGrantedPerms)
                }
            }
        }

        private fun hasRationale(): Boolean {
            return !mRationale.isNullOrEmpty()
        }

        private fun hasNeverAsk(notGrantedPerms: Array<String>): Boolean {
            return notGrantedPerms.any {
                !ActivityCompat.shouldShowRequestPermissionRationale(mActivity, it)
            }
        }

        private fun confirmRequest(allPerms: Array<String>, notGrantedPerms: Array<String>) {
            mRationale?.apply {
                mActivity.confirm(title = mActivity.getString(R.string.aclin_permission_request_title), message = this, ok = {
                    request(allPerms, notGrantedPerms)
                }, cancel = {
                    mDeniedCallback?.invoke(notGrantedPerms)
                })
            }
        }

        private fun request(allPerms: Array<String>, notGrantedPerms: Array<String>) {
            mRequestCode++
            PermissionFragment.injectIfNeededIn(mActivity)
            mActivity.getPresenter<PermissionPresenter>().apply {
                response.observe(mOwner, ResponseObserver(mRequestCode, response, mGrantedCallback ?: {}, mDeniedCallback ?: {}))
                request.postValue(Request(mRequestCode, allPerms, notGrantedPerms))
            }
        }
    }

    class Request(val requestCode: Int, val allPerms: Array<String>, val notGrantedPerms: Array<String>)
    class Response(val requestCode: Int, val grant: Boolean, val allPerms: Array<String>, val deniedPerms: Array<String>)

    class ResponseObserver(private val mRequestCode: Int,
                           private val mLiveData: LiveData<Response>,
                           private val mGrantedCallback: PermissionCallback,
                           private val mDeniedCallback: PermissionCallback) : Observer<Response> {
        override fun onChanged(value: Response?) {
            value?.apply {
                if (mRequestCode == requestCode) {
                    if (grant) {
                        mGrantedCallback(allPerms)
                    } else {
                        mDeniedCallback(deniedPerms)
                    }
                    mLiveData.removeObserver(this@ResponseObserver)
                }
            }
        }
    }
}

