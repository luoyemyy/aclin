package com.github.luoyemyy.aclin.image.crop

class CropImage(val srcPath: String, var ratio: Float = 1f, var cropPath: String? = null, var crop: Boolean = false)

class CropArgs(val images: List<CropImage> = listOf(), val fixedRatio: Boolean = false, val ratio: Float = 1f)