package com.github.luoyemyy.aclin.image.picker.camera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.ext.uri
import com.github.luoyemyy.aclin.mvp.ext.getPresenter

class CameraFragment : Fragment(), Observer<CameraBuilder.Request> {

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "com.github.luoyemyy.aclin.image.picker.camera.CameraFragment"

        fun injectIfNeededIn(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            if (manager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) == null) {
                manager.beginTransaction().add(CameraFragment(), PERMISSION_FRAGMENT_TAG).commit()
                manager.executePendingTransactions()
            }
        }
    }

    private lateinit var mPresenter: CameraPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter = requireActivity().getPresenter()
        mPresenter.request.observe(this, this)
    }

    override fun onChanged(value: CameraBuilder.Request?) {
        value?.apply {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, requireContext().uri(path))
                    startActivityForResult(takePictureIntent, requestCode)
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val value = mPresenter.request.value ?: return
        if (requestCode == value.requestCode && resultCode == Activity.RESULT_OK) {
            mPresenter.response.postValue(CameraBuilder.Response(requestCode, true, value.path))
        }
    }
}