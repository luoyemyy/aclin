package com.github.luoyemyy.aclin.image.picker.gallery

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.mvp.core.DataItem
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter

class GalleryPresenter(var mApp: Application) : MvpPresenter(mApp) {

    private var mBuckets: List<Bucket>? = null
    private val mModel = GalleryModel(mApp)
    private var mMinSelect: Int = 0
    private var mMaxSelect: Int = 0
    private var selectBucket: Bucket? = null
    val menuLiveData = MutableLiveData<Boolean>()
    val titleLiveData = MutableLiveData<Boolean>()
    val imageLiveData = ListLiveData<Image> { DataItem(it) }
    val bucketLiveData = ListLiveData<Bucket> { DataItem(it) }

    override fun loadData(bundle: Bundle?) {
        mMinSelect = GalleryBuilder.parseMinSelect(bundle)
        mMaxSelect = GalleryBuilder.parseMaxSelect(bundle)
        mBuckets = mModel.getBuckets()
        setDefaultBucket()
        imageLiveData.loadStart(selectBucket?.images)
        bucketLiveData.loadStart(mBuckets)
    }

    private fun countSelect(): Int {
        return mBuckets?.firstOrNull()?.images?.count { it.select } ?: 0
    }

    fun getMenu(): Pair<Boolean, String> {
        val count = countSelect()
        return Pair(count in mMinSelect..mMaxSelect, mApp.getString(R.string.aclin_image_picker_gallery_menu_sure, count, mMaxSelect))
    }

    fun getTitle(): String {
        return selectBucket?.name ?: ""
    }

    private fun setDefaultBucket() {
        mBuckets?.find { it.id == GalleryModel.BUCKET_ALL }?.also {
            selectBucket(it)
        }
    }

    private fun selectBucket(bucket: Bucket) {
        selectBucket?.select = false
        selectBucket = bucket
        selectBucket?.select = true
        titleLiveData.postValue(true)
    }

    fun selectBucket(id: String?) {
        id?.also {
            mBuckets?.find { it.id == id }?.also { bucket ->
                selectBucket(bucket)
                imageLiveData.loadRefresh(selectBucket?.images)
            }
        }
    }

    fun selectImage(image: Image, select: Boolean): Boolean {
        return if (select) {
            val count = countSelect()
            if (count >= mMaxSelect) {
                mApp.toast(mApp.getString(R.string.aclin_image_picker_gallery_select_limit_max, mMaxSelect))
                false
            } else {
                image.select = true
                menuLiveData.postValue(true)
                managerSelectImage(image)
                select
            }
        } else {
            image.select = false
            menuLiveData.postValue(true)
            managerSelectImage(image)
            select
        }
    }

    fun getSelectImages(): ArrayList<String> {
        return mBuckets?.find { it.id == GalleryModel.BUCKET_SELECT }?.images?.mapTo(arrayListOf()) { it.path } ?: arrayListOf()
    }

    private fun managerSelectImage(image: Image) {
        mBuckets?.find { it.id == GalleryModel.BUCKET_SELECT }?.images?.apply {
            if (image.select) {
                if (!contains(image)) {
                    add(image)
                }
            } else {
                remove(image)
            }
        }
    }

    fun previewImages(position: Int, select: Boolean, callback: (Int, ArrayList<String>) -> Unit) {
        if (select) {
            mBuckets?.find { it.id == GalleryModel.BUCKET_SELECT }?.images
        } else {
            selectBucket?.images
        }?.mapTo(arrayListOf()) { it.path }?.apply {
            if (this.isNotEmpty()) {
                callback(position, this)
            }
        }
    }
}