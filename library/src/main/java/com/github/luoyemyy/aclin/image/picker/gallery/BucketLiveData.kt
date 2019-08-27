package com.github.luoyemyy.aclin.image.picker.gallery

import android.app.Application
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.image.ImagePicker
import com.github.luoyemyy.aclin.mvp.*
import java.io.File

class BucketLiveData(private val mApp: Application) : ListLiveData() {

    companion object {
        private const val BUCKET_ALL = "bucketAll"
    }

    private val mBuckets: MutableList<Bucket> = mutableListOf()
    private val mBucketMap: MutableMap<String, Bucket> = mutableMapOf()
    private var mGalleryArgs = ImagePicker.parseGalleryArgs(null)

    val selectBucketLiveData = MutableLiveData<Bucket>()
    val menuLiveData = MutableLiveData<Boolean>()

    val imageLiveData = object : ListLiveData() {
        override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return mBucketMap[getSelectBucketId()]?.images
        }
    }

    private val mContentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            loadRefresh()
        }
    }

    fun changeImage(position: Int, select: Boolean): Boolean {
        val image = getSelectBucket()?.let {
            if (position in 0 until it.images.size) {
                it.images[position]
            } else {
                null
            }
        } ?: let {
            return false
        }

        if (select && !image.select && countSelectImage() >= mGalleryArgs.maxSelect) {
            mApp.toast(mApp.getString(R.string.aclin_image_picker_gallery_select_limit_max, mGalleryArgs.maxSelect))
            return false
        }
        image.select = select
        menuLiveData.value = true
        return select
    }

    fun countSelectImage(): Int {
        return mBucketMap[BUCKET_ALL]?.images?.count { it.select } ?: 0
    }

    fun submitImageText(): String {
        return mApp.getString(R.string.aclin_image_picker_gallery_menu_sure, countSelectImage(), mGalleryArgs.maxSelect)
    }

    fun selectImages(): ArrayList<String>? {
        return mBucketMap[BUCKET_ALL]?.images?.filter { it.select }?.mapTo(arrayListOf()) { it.path } ?: let {
            //            mApp.toast()
            null
        }
    }

    fun changeBucket(position: Int) {
        var selectPosition = -1
        var select: Bucket? = null
        mBuckets.forEachIndexed { index, bucket ->
            if (bucket.select) {
                selectPosition = index
            }
            bucket.select = index == position
            if (bucket.select) {
                select = bucket
            }
        }
        if (selectPosition != position) {
            value = DataItemGroup(true, mBuckets)
            imageLiveData.loadRefresh()
        }
        select?.apply {
            selectBucketLiveData.value = select
        }
    }

    override fun onActive() {
        mApp.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mContentObserver
        )
    }

    override fun onInactive() {
        mApp.contentResolver.unregisterContentObserver(mContentObserver)
    }

    override fun loadData(bundle: Bundle?, search: String?, paging: Paging, loadType: LoadType): List<DataItem>? {
        if (loadType.isInit()) {
            mGalleryArgs = ImagePicker.parseGalleryArgs(bundle)
        }
        load()
        return mBuckets
    }

    override fun loadInitAfter(ok: Boolean, items: List<DataItem>): List<DataItem> {
        imageLiveData.loadInit(null)
        return super.loadInitAfter(ok, items)
    }

    private fun getSelectBucketId(): String? {
        return getSelectBucket()?.id
    }

    private fun getSelectBucket(): Bucket? {
        return mBuckets.firstOrNull { it.select }
    }

    private fun load() {

        val data = mApp.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf("_data", "bucket_id", "bucket_display_name", "date_added"),
            "mime_type like '%image/jp%' and _size > 0 ",
            null,
            "date_added DESC"
        )

        val buckets = mutableListOf<Bucket>()
        val bucketMap = mutableMapOf<String, Bucket>()

        val bucketAll = Bucket(BUCKET_ALL, mApp.getString(R.string.aclin_image_picker_gallery_bucket_all))
        buckets.add(bucketAll)
        bucketMap[bucketAll.id] = bucketAll

        if (data != null) {
            while (data.moveToNext()) {
                val bucketId = data.getString(data.getColumnIndex("bucket_id"))
                val bucketName = data.getString(data.getColumnIndex("bucket_display_name"))
                val path = data.getString(data.getColumnIndex("_data"))
                if (!path.isNullOrEmpty() && File(path).exists()) {
                    val image = Image(path)
                    if (!bucketId.isNullOrEmpty() && !bucketName.isNullOrEmpty()) {
                        (bucketMap[bucketId] ?: Bucket(bucketId, bucketName).apply {
                            bucketMap[bucketId] = this
                            buckets.add(this)
                        }).images.add(image)
                    }
                    bucketAll.images.add(image)
                }
            }
        }
        data?.close()

        (selectBucketLiveData.value?.apply { buckets.first { it.id == id } } ?: bucketAll).apply {
            select = true
            selectBucketLiveData.postValue(this)
        }

        mBuckets.clear()
        mBuckets.addAll(buckets)
        mBucketMap.clear()
        mBucketMap.putAll(bucketMap)
    }
}