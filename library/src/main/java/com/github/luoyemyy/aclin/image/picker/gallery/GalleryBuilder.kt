package com.github.luoyemyy.aclin.image.picker.gallery

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus

class GalleryBuilder private constructor() {

    companion object {
        const val MIN_SELECT = "minSelect"
        const val MAX_SELECT = "maxSelect"
        const val PICKER_RESULT = "gallery_picker_result"

        internal fun parseGalleryArgs(bundle: Bundle?): GalleryArgs {
            return bundle?.let {
                GalleryArgs(
                    it.getInt(MIN_SELECT, 1),
                    it.getInt(MAX_SELECT, 9)
                )
            } ?: GalleryArgs(1, 9)
        }
    }

    private lateinit var mFragment: Fragment
    private var mGalleryCallback: GalleryCallback? = null
    private var mActionId: Int = 0
    private var mMin = 1
    private var mMax = 9

    constructor(fragment: Fragment) : this() {
        mFragment = fragment
    }

    fun callback(callback: GalleryCallback): GalleryBuilder {
        mGalleryCallback = callback
        return this
    }

    fun selectCount(min: Int, max: Int): GalleryBuilder {
        mMin = min
        mMax = max
        return this
    }

    fun buildAndPicker(actionId: Int) {
        mActionId = actionId
        setBus(mFragment, PICKER_RESULT, BusResult {
            it.extra?.getStringArrayList(PICKER_RESULT)?.apply {
                mGalleryCallback?.invoke(this)
            }
        })
        mFragment.findNavController().navigate(mActionId, bundleOf(MIN_SELECT to mMin, MAX_SELECT to mMax))
    }

}