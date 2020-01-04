package com.github.luoyemyy.aclin.image.crop

import com.github.luoyemyy.aclin.mvp.core.MvpData

class CropImage(val srcPath: String, var ratio: Float = 1f, var customRatio: String? = null, var cropPath: String? = null, var crop: Boolean = false) : MvpData() {

    fun path(): String? {
        return if (crop) cropPath else srcPath
    }

}

class CropArgs(val images: List<CropImage> = listOf(), val fixedRatio: Boolean = false)