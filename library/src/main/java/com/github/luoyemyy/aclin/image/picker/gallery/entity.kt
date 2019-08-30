package com.github.luoyemyy.aclin.image.picker.gallery

import com.github.luoyemyy.aclin.mvp.DataItem

class Image(var path: String, var select: Boolean = false) : DataItem()

data class Bucket(var id: String, var name: String, var select: Boolean = false,
                  var images: MutableList<Image> = mutableListOf()) : DataItem()

data class GalleryArgs(val minSelect: Int, val maxSelect: Int, val crop: Boolean, val cropRatio: Float)