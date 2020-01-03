package com.github.luoyemyy.aclin.image.picker.gallery

class Image(var path: String, var select: Boolean = false)

class Bucket(var id: String, var name: String, var select: Boolean = false, var images: MutableList<Image> = mutableListOf())

class GalleryArgs(val minSelect: Int, val maxSelect: Int)