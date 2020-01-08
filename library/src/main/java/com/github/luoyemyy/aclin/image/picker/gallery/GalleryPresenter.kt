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
    private var mSelectBucket: Bucket? = null
    private var mMenuLiveData: MutableLiveData<Boolean>? = null
    private var mTitleLiveData: MutableLiveData<Boolean>? = null
    private var mImageLiveData: ListLiveData<Image>? = null
    private var mBucketLiveData: ListLiveData<Bucket>? = null

    override fun loadData(bundle: Bundle?) {
        mMinSelect = GalleryBuilder.parseMinSelect(bundle)
        mMaxSelect = GalleryBuilder.parseMaxSelect(bundle)
        mBuckets = mModel.getBuckets()
        selectDefaultBucket()
        imageLiveData().loadStart(mSelectBucket?.images)
    }

    public override fun clear() {
        super.clear()
        mBuckets = null
        mSelectBucket = null
        mMenuLiveData = null
        mTitleLiveData = null
        mImageLiveData = null
        mBucketLiveData = null
    }

    fun menuLiveData(): MutableLiveData<Boolean> {
        return mMenuLiveData ?: MutableLiveData<Boolean>().apply { mMenuLiveData = this }
    }

    fun titleLiveData(): MutableLiveData<Boolean> {
        return mTitleLiveData ?: MutableLiveData<Boolean>().apply { mTitleLiveData = this }
    }

    fun imageLiveData(): ListLiveData<Image> {
        return mImageLiveData ?: ListLiveData<Image> { DataItem(it) }.apply { mImageLiveData = this }
    }

    fun bucketLiveData(): ListLiveData<Bucket> {
        return mBucketLiveData ?: ListLiveData<Bucket> { DataItem(it) }.apply { mBucketLiveData = this }
    }

    private fun countSelect(): Int {
        return mBuckets?.firstOrNull()?.images?.count { it.select } ?: 0
    }

    fun getMenu(): Pair<Boolean, String> {
        val count = countSelect()
        return Pair(count in mMinSelect..mMaxSelect, mApp.getString(R.string.aclin_image_picker_gallery_menu_sure, count, mMaxSelect))
    }

    fun getTitle(): String {
        return mSelectBucket?.name ?: mApp.getString(R.string.aclin_image_picker_gallery_bucket_all)
    }

    fun loadBuckets() {
        bucketLiveData().loadStart(mBuckets)
    }

    private fun selectDefaultBucket() {
        selectBucket(GalleryModel.BUCKET_ALL)
    }

    fun selectBucket(id: String?) {
        id?.also {
            mBuckets?.find { it.id == id }?.also { bucket ->
                mSelectBucket?.select = false
                mSelectBucket = bucket
                mSelectBucket?.select = true
                titleLiveData().postValue(true)
                imageLiveData().loadRefresh(mSelectBucket?.images)
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
                menuLiveData().postValue(true)
                managerSelectImage(image)
                select
            }
        } else {
            image.select = false
            menuLiveData().postValue(true)
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
            mSelectBucket?.images
        }?.mapTo(arrayListOf()) { it.path }?.apply {
            if (this.isNotEmpty()) {
                callback(position, this)
            }
        }
    }
}