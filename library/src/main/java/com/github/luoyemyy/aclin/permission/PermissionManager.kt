@file:Suppress("unused")

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.ext.confirm

object PermissionManager {

    private var mRequestCode = 1
    internal const val EVENT_REQUEST = "REQUEST_PERMISSION"
    internal const val EVENT_RESPONSE = "RESPONSE_PERMISSION"
    internal const val KEY_REQUEST_CODE = "request_code"
    internal const val KEY_ALL_PERMS = "all_perms"
    internal const val KEY_NOT_PERMS = "not_perms"
    internal const val KEY_DENIED_PERMS = "denied_perms"

    fun toSetting(fragment: Fragment, msg: String) {
        toSetting(fragment.requireActivity(), msg)
    }

    private fun toSetting(activity: FragmentActivity, msg: String) {
        activity.apply {
            confirm(title = getString(R.string.aclin_permission_failure_title), message = msg, okText = R.string.aclin_permission_to_setting, ok = {
                toSetting(this)
            })
        }
    }

    private fun toSetting(activity: FragmentActivity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", activity.packageName, null))
        activity.startActivityForResult(intent, 1)
    }

    /**
     * 过滤出还未授权的权限
     */
    fun filterNotGrantedPermissions(context: Context, @Size(min = 1) vararg perms: String): Array<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return arrayOf()
        }
        return perms.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()
    }

    class Callback(private val mRequestCode: Int) : BusResult {

        internal var granted: PermissionCallback? = null

        internal var denied: PermissionCallback? = null

        override fun busResult(msg: BusMsg) {
            if (msg.event == EVENT_RESPONSE) {
                msg.extra?.apply {
                    val requestCode = getInt(KEY_REQUEST_CODE)
                    if (mRequestCode == requestCode) {
                        val allPerms = getStringArray(KEY_ALL_PERMS) ?: arrayOf()
                        val deniedPerms = getStringArray(KEY_DENIED_PERMS) ?: arrayOf()
                        if (deniedPerms.isEmpty()) {
                            granted?.invoke(allPerms)
                        } else {
                            denied?.invoke(deniedPerms)
                        }
                    }
                }
            }
        }
    }

    class Builder internal constructor(private val mFragment: Fragment,
                                       private val mRationale: String? = null,
                                       vararg perms: String) {

        private val mAllPerms: Array<String> = perms.toList().toTypedArray()
        private val mNotGrantedPerms: Array<String> = filterNotGrantedPermissions(mFragment.requireContext(), *perms)
        private var mCallback: Callback = Callback(++mRequestCode)

        fun granted(callback: PermissionCallback): Builder {
            mCallback.granted = callback
            return this
        }

        fun denied(callback: PermissionCallback): Builder {
            mCallback.denied = callback
            return this
        }

        fun request() {
            if (mNotGrantedPerms.isNullOrEmpty()) {
                mCallback.granted?.invoke(mAllPerms)
            } else {
                if (showRationale()) {
                    rationaleRequest()
                } else {
                    baseRequest()
                }
            }
        }

        private fun showRationale(): Boolean {
            return !mRationale.isNullOrEmpty() && mNotGrantedPerms.any { ActivityCompat.shouldShowRequestPermissionRationale(mFragment.requireActivity(), it) }
        }

        private fun rationaleRequest() {
            mFragment.requireActivity().confirm(title = mFragment.requireContext().getString(R.string.aclin_permission_request_title), message = mRationale, ok = {
                baseRequest()
            }, cancel = {
                mCallback.denied?.invoke(mNotGrantedPerms)
            })
        }

        private fun baseRequest() {
            PermissionFragment.injectIfNeededIn(mFragment.requireActivity())
            mFragment.setBus(mCallback, EVENT_RESPONSE)
            mFragment.postBus(EVENT_REQUEST, extra = bundleOf(KEY_REQUEST_CODE to mRequestCode, KEY_ALL_PERMS to mAllPerms, KEY_NOT_PERMS to mNotGrantedPerms))
        }
    }
}

