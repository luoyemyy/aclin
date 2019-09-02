package com.github.luoyemyy.aclin.permission

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.mvp.getPresenter

class PermissionFragment : Fragment(), Observer<PermissionManager.Request> {

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

    private lateinit var mPresenter: PermissionPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter = requireActivity().getPresenter()
        mPresenter.request.observe(this, this)
    }

    override fun onChanged(value: PermissionManager.Request?) {
        value?.apply {
            if (!notGrantedPerms.isNullOrEmpty()) {
                requestPermissions(notGrantedPerms, id)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val value = mPresenter.request.value ?: return
        if (value.requestCode == requestCode) {
            val deniedPerms = permissions
                    .filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
                    .toTypedArray()
            mPresenter.response.postValue(PermissionManager.Response(value.requestCode, deniedPerms.isEmpty(), value.allPerms, deniedPerms))
        }
    }
}