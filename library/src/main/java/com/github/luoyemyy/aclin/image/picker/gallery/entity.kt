package com.github.luoyemyy.aclin.image.picker.gallery

import com.github.luoyemyy.aclin.mvp.core.MvpData

class Image(var path: String, var select: Boolean = false) : MvpData()

class Bucket(var id: String, var name: String, var select: Boolean = false, var images: MutableList<Image> = mutableListOf()) : MvpData()

class GalleryArgs(val minSelect: Int, val maxSelect: Int)