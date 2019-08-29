package com.github.luoyemyy.aclin.image.picker.gallery

import android.app.Application
import com.github.luoyemyy.aclin.image.calculateImageItemSize
import com.github.luoyemyy.aclin.mvp.AbsPresenter

class GalleryPresenter(private var mApp: Application) : AbsPresenter(mApp) {

    private var mImageInfo: Pair<Int, Int>? = null

    val bucketsLiveData = BucketLiveData(mApp)
    val menuLiveData = bucketsLiveData.menuLiveData
    val selectBucketLiveData = bucketsLiveData.selectBucketLiveData
    val imagesLiveData = bucketsLiveData.imageLiveData

    fun getImageSpan(): Int = getImageInfo().first

    fun getImageSize(): Int = getImageInfo().second

    private fun getImageInfo() = mImageInfo ?: calculateImageItemSize(mApp).apply { mImageInfo = this }

}