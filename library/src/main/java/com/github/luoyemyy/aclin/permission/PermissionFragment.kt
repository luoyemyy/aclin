package com.github.luoyemyy.aclin.permission

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.bus.setBus

class PermissionFragment : Fragment(), BusResult {

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "com.github.luoyemyy.aclin.permission.PermissionFragment"

        fun injectIfNeededIn(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            if (manager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) == null) {
                manager.beginTransaction().add(PermissionFragment(), PERMISSION_FRAGMENT_TAG).commit()
                manager.executePendingTransactions()
            }
        }
    }

    private var mRequestCode: Int = 0
    private lateinit var mAllPerms: Array<String>
    private lateinit var mNotPerms: Array<String>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setBus(this, PermissionManager.EVENT_REQUEST)
    }

    override fun busResult(msg: BusMsg) {
        if (msg.event == PermissionManager.EVENT_REQUEST) {
            msg.extra?.apply {
                mRequestCode = getInt(PermissionManager.KEY_REQUEST_CODE)
                mAllPerms = getStringArray(PermissionManager.KEY_ALL_PERMS) ?: return
                mNotPerms = getStringArray(PermissionManager.KEY_NOT_PERMS) ?: return
                if (mNotPerms.isNotEmpty()) {
                    requestPermissions(mNotPerms, mRequestCode)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (mRequestCode == requestCode) {
            val deniedPerms = permissions
                    .filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
                    .toTypedArray()
            postBus(PermissionManager.EVENT_RESPONSE, extra = bundleOf(
                PermissionManager.KEY_REQUEST_CODE to mRequestCode,
                PermissionManager.KEY_ALL_PERMS to mAllPerms,
                PermissionManager.KEY_DENIED_PERMS to deniedPerms))
        }
    }
}