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

        internal fun parseMinSelect(bundle: Bundle?): Int {
            return bundle?.getInt(MIN_SELECT, 1) ?: 1
        }

        internal fun parseMaxSelect(bundle: Bundle?): Int {
            return bundle?.getInt(MAX_SELECT, 9) ?: 9
        }
    }

    private lateinit var mFragment: Fragment
    private var mMin = 1
    private var mMax = 9
    private var mActionId = 0

    constructor(fragment: Fragment) : this() {
        mFragment = fragment
    }

    fun action(actionId: Int): GalleryBuilder {
        mActionId = actionId
        return this
    }

    fun selectCount(min: Int, max: Int): GalleryBuilder {
        mMin = min
        mMax = max
        return this
    }

    fun buildAndPicker(callback: GalleryCallback) {
        mFragment.setBus(BusResult {
            it.extra?.getStringArrayList(PICKER_RESULT)?.apply {
                callback(this)
            }
        }, PICKER_RESULT)
        mFragment.findNavController().navigate(mActionId, bundleOf(MIN_SELECT to mMin, MAX_SELECT to mMax))
    }

}