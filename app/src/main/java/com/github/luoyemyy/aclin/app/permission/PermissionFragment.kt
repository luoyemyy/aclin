package com.github.luoyemyy.aclin.app.permission

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.aclin.app.databinding.FragmentPermissionBinding
import com.github.luoyemyy.aclin.ext.toast
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class PermissionFragment : Fragment(), View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private lateinit var mBinding: FragmentPermissionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPermissionBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.btnPermission.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        EasyPermissions.requestPermissions(
            PermissionRequest.Builder(
                this,
                1,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).setRationale("需要读写文件权限").build()
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            AppSettingsDialog.Builder(this).setTitle("设置读写文件权限").setRationale("设置读写文件权限").build().show()
        } else {
            requireContext().toast("需要读写文件权限")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        requireContext().toast("已取得权限")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}