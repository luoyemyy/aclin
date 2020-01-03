package com.github.luoyemyy.aclin.image.picker.gallery

import android.app.Application
import android.os.Bundle
import com.github.luoyemyy.aclin.mvp.MvpPresenter

class GalleryPresenter(private var mApp: Application) : MvpPresenter(mApp) {

    private var mImageInfo: Pair<Int, Int>? = null

    val bucketsLiveData = BucketLiveData(mApp)
    val menuLiveData = bucketsLiveData.menuLiveData
    val selectBucketLiveData = bucketsLiveData.selectBucketLiveData
    val imagesLiveData = bucketsLiveData.imageLiveData

    fun getImageSpan(): Int = getImageInfo().first

    fun getImageSize(): Int = getImageInfo().second

    private fun getImageInfo() = mImageInfo ?: calculateImageItemSize(mApp).apply { mImageInfo = this }

    override fun loadData(bundle: Bundle?) {
        bucketsLiveData.setArgs(bundle)
        bucketsLiveData.loadStart()
    }
}