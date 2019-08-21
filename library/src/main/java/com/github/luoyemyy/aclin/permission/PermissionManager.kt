package com.github.luoyemyy.aclin.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Size
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.mvp.getPresenter


object PermissionManager {

    fun toSetting(fragment: Fragment, msg: String) {
        showDialog(fragment.requireContext(), R.string.aclin_permission_failure_title, msg, R.string.aclin_permission_to_setting) {
            toSetting(fragment.requireActivity())
        }
    }

    fun toSetting(activity: FragmentActivity, msg: String) {
        showDialog(activity, R.string.aclin_permission_failure_title, msg, R.string.aclin_permission_to_setting) {
            toSetting(activity)
        }
    }

    private fun toSetting(activity: FragmentActivity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", activity.packageName, null))
        activity.startActivityForResult(intent, 1)
    }

    private fun showDialog(context: Context, title: Int, msg: String, okText: Int = android.R.string.ok, okCallback: () -> Unit) {
        AlertDialog.Builder(context).setCancelable(false)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(okText) { _, _ ->
                okCallback()
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
        private var mRationale: String? = null

        constructor(fragment: Fragment, rationale: String? = null) : this() {
            mContext = fragment.requireContext()
            mOwner = fragment
            mAct = fragment.requireActivity()
            mRationale = rationale
        }

        constructor(activity: FragmentActivity, rationale: String? = null) : this() {
            mContext = activity
            mOwner = activity
            mAct = activity
            mRationale = rationale
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
            val allPerms = perms.toList().toTypedArray()
            val notGrantedPerms = filterNotGrantedPermissions(mContext, *perms)
            if (notGrantedPerms.isNullOrEmpty()) {
                mGranted?.invoke(allPerms)
            } else {
                if (!mRationale.isNullOrEmpty() && !hasNeverAsk(notGrantedPerms)) {
                    confirmRequest(allPerms, notGrantedPerms)
                } else {
                    request(allPerms, notGrantedPerms)
                }
            }
        }

        private fun hasNeverAsk(notGrantedPerms: Array<String>): Boolean {
            return notGrantedPerms.any {
                !ActivityCompat.shouldShowRequestPermissionRationale(mAct, it)
            }
        }

        private fun confirmRequest(allPerms: Array<String>, notGrantedPerms: Array<String>) {
            mRationale?.apply {
                showDialog(mContext, R.string.aclin_permission_request_title, this) {
                    request(allPerms, notGrantedPerms)
                }
            }
        }

        private fun request(allPerms: Array<String>, notGrantedPerms: Array<String>) {
            PermissionFragment.injectIfNeededIn(mAct)
            mAct.getPresenter<PermissionPresenter>().also {
                it.callback(mOwner, mGranted ?: {}, mDenied ?: {})
                it.request(allPerms, notGrantedPerms)
            }
        }
    }
}

