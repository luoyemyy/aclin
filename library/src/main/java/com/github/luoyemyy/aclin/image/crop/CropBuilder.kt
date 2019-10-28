package com.github.luoyemyy.aclin.image.crop

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus

class CropBuilder private constructor() {

    companion object {
        const val RATIO_1_1 = 1 / 1f
        const val RATIO_4_3 = 4 / 3f
        const val RATIO_3_4 = 3 / 4f
        const val RATIO_16_9 = 19 / 9f
        const val RATIO_9_16 = 9 / 16f
        const val PATHS = "paths"
        const val RATIO_FIXED = "ratio_fixed"
        const val RATIO = "ratio"
        const val CROP_RESULT = "crop_result"

        internal fun parseCropArgs(args: Bundle?): CropArgs {
            return args?.let { bundle ->
                val ratio = bundle.getFloat(RATIO, 1f)
                val fixed = bundle.getBoolean(RATIO_FIXED, false)
                val images = bundle.getStringArrayList(PATHS)?.map { CropImage(it, ratio) } ?: listOf()
                CropArgs(images, fixed)
            } ?: CropArgs()
        }
    }

    private lateinit var mFragment: Fragment
    private lateinit var mPaths: ArrayList<String>
    private var mCropCallback: CropCallback? = null
    private var mRatioFixed = false
    private var mRatio = 1f
    private var mActionId = 0

    constructor(fragment: Fragment) : this() {
        mFragment = fragment
    }

    fun callback(callback: CropCallback): CropBuilder {
        mCropCallback = callback
        return this
    }

    fun ratio(ratioFixed: Boolean, ratio: Float): CropBuilder {
        mRatioFixed = ratioFixed
        mRatio = ratio
        return this
    }

    fun paths(paths: ArrayList<String>): CropBuilder {
        mPaths = paths
        return this
    }

    fun action(actionId: Int): CropBuilder {
        mActionId = actionId
        return this
    }

    fun buildAndCrop() {
        setBus(mFragment, CROP_RESULT, BusResult {
            it.extra?.getStringArrayList(CROP_RESULT)?.apply {
                mCropCallback?.invoke(this)
            }
        })
        mFragment.findNavController().navigate(mActionId, bundleOf(
            PATHS to mPaths,
            RATIO_FIXED to mRatioFixed,
            RATIO to mRatio))
    }

}