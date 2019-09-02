package com.github.luoyemyy.aclin.image.picker.camera

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.aclin.logger.loge
import com.github.luoyemyy.aclin.mvp.getPresenter
import java.util.*


class CameraBuilder private constructor() {

    companion object {
        private var mRequestCode = 1
    }

    private var mCameraCallback: CameraCallback? = null

    private lateinit var mContext: Context
    private lateinit var mOwner: LifecycleOwner
    private lateinit var mActivity: FragmentActivity

    constructor(fragment: Fragment) : this() {
        mContext = fragment.requireContext()
        mOwner = fragment
        mActivity = fragment.requireActivity()
    }

    constructor(activity: FragmentActivity) : this() {
        mContext = activity
        mOwner = activity
        mActivity = activity
    }

    fun callback(callback: CameraCallback): CameraBuilder {
        mCameraCallback = callback
        return this
    }

    fun buildAndCapture() {

        FileManager.getInstance().image("image_${Date().time}")?.apply {
            mRequestCode++
            CameraFragment.injectIfNeededIn(mActivity)
            mActivity.getPresenter<CameraPresenter>().apply {
                response.observe(mOwner, ResponseObserver(mRequestCode, response, mCameraCallback ?: {}))
                request.postValue(Request(mRequestCode, absolutePath))
            }
        } ?: run {
            loge("CameraBuilder", "创建图片失败")
        }
    }

    class Request(val requestCode: Int, val path: String)
    class Response(val requestCode: Int, val success: Boolean, val path: String)

    class ResponseObserver(private val mRequestCode: Int,
                           private val mLiveData: LiveData<Response>,
                           private val mCameraCallback: CameraCallback) : Observer<Response> {
        override fun onChanged(value: Response?) {
            value?.apply {
                if (mRequestCode == requestCode) {
                    if (success) {
                        mCameraCallback(path)
                    }
                    mLiveData.removeObserver(this@ResponseObserver)
                }
            }
        }
    }
}

