package com.github.luoyemyy.aclin.app.permission

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.aclin.app.databinding.FragmentPermissionBinding
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.permission.PermissionManager

class PermissionFragment : Fragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentPermissionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPermissionBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.btnPermission.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        PermissionManager.Builder(this)
            .granted {
                requireContext().toast("已取得权限")
            }
            .denied {
                if (Manifest.permission.CAMERA in it) {
                    PermissionManager.toSetting(this, "使用拍照功能需要相机权限")
                } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE in it) {
                    PermissionManager.toSetting(this, "使用拍照功能需要读写文件权限")
                }
            }
            .buildAndRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}