package com.github.luoyemyy.aclin.image

import android.os.Bundle
import androidx.core.os.bundleOf
import com.github.luoyemyy.aclin.image.picker.gallery.GalleryArgs

object ImagePicker {

    const val MAX_SELECT = "maxSelect"
    const val MIN_SELECT = "minSelect"
    const val CROP = "crop"
    const val CROP_RATIO = "cropRatio"

    fun galleryArgs(max: Int = 9, min: Int = 1, crop: Boolean = false, cropRatio: Float = 1f): Bundle {
        val bundle = bundleOf(CROP to crop, CROP_RATIO to cropRatio)
        val minSelect = if (min < 1) 1 else min
        bundle.putInt(MIN_SELECT, minSelect)
        val maxSelect = if (max < minSelect) minSelect else max
        bundle.putInt(MAX_SELECT, maxSelect)
        return bundle
    }


    internal fun parseGalleryArgs(bundle: Bundle?): GalleryArgs {
        return bundle?.let {
            GalleryArgs(
                it.getInt(MIN_SELECT, 1), it.getInt(MAX_SELECT, 9), it.getBoolean(
                    CROP, false), it.getFloat(CROP_RATIO, 1f)
            )
        } ?: GalleryArgs(1, 9, false, 1f)
    }
}