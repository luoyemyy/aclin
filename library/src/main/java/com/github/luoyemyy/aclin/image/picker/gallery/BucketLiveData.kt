package com.github.luoyemyy.aclin.image.picker.gallery

import android.app.Application
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.R
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.image.ImagePicker
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.aclin.mvp.ListLiveData
import com.github.luoyemyy.aclin.mvp.LoadType
import com.github.luoyemyy.aclin.mvp.Paging
import java.io.File

class BucketLiveData(private val mApp: Application) : ListLiveData() {

    companion object {
        const val BUCKET_ALL = "bucketAll"
        const val BUCKET_SELECT = "bucketSelect"
    }

    private var mBuckets: MutableList<Bucket> = mutableListOf()
    private var mBucketMap: MutableMap<String, Bucket> = mutableMapOf()
    private var mGalleryArgs = ImagePicker.parseGalleryArgs(null)

    val menuLiveData = MutableLiveData<Boolean>()

    val selectBucketLiveData = object : MutableLiveData<Bucket>() {
        override fun onActive() {
            mApp.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mContentObserver
            )
        }

        override fun onInactive() {
            mApp.contentResolver.unregisterContentObserver(mContentObserver)
        }
    }

    val imageLiveData = object : ListLiveData() {
        override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
            return selectBucketLiveData.value?.let { mBucketMap[it.id]?.images }
        }
    }

    private val mContentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            loadRefresh()
        }
    }

    override fun loadData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<DataItem>? {
        if (loadType.isInit()) {
            mGalleryArgs = ImagePicker.parseGalleryArgs(bundle)
        }
        load()
        return mBuckets
    }

    private fun countSelectImage(): Int {
        return selectedImages0()?.size ?: 0
    }

    fun enableSubmit(): Boolean {
        return countSelectImage() in (mGalleryArgs.minSelect..mGalleryArgs.maxSelect)
    }

    fun submitMenuText(): String {
        return mApp.getString(R.string.aclin_image_picker_gallery_menu_sure, countSelectImage(), mGalleryArgs.maxSelect)
    }

    private fun selectedImages0(): List<Image>? {
        return mBucketMap[BUCKET_SELECT]?.images
    }

    fun selectedImages(): ArrayList<String>? {
        return selectedImages0()?.mapTo(arrayListOf()) { it.path } ?: let {
            null
        }
    }

    fun previewImages(position: Int, callback: (Int, ArrayList<String>) -> Unit) {
        mBucketMap[selectBucketLiveData.value?.id]?.images?.mapTo(arrayListOf()) { it.path }?.apply {
            callback(position, this)
        }
    }

    fun selectImage(position: Int, select: Boolean): Boolean {
        val image = mBucketMap[selectBucketLiveData.value?.id]?.images?.get(position) ?: return false
        if (select && !image.select && countSelectImage() >= mGalleryArgs.maxSelect) {
            mApp.toast(mApp.getString(R.string.aclin_image_picker_gallery_select_limit_max, mGalleryArgs.maxSelect))
            return false
        }
        image.select = select
        updateSelectImages()
        menuLiveData.value = true
        return select
    }

    private fun updateSelectImages() {
        mBucketMap[BUCKET_SELECT]?.images = mBucketMap[BUCKET_ALL]?.images?.filterTo(mutableListOf()) { it.select } ?: mutableListOf()
        itemChange { _, _ ->
            mBucketMap[BUCKET_SELECT]?.hasPayload()
            true
        }
    }

    /**
     * 选中分类
     * @param bucket 当前选中的分类，如果为null,则默认为全部图片
     */
    fun selectBucket(bucket: Bucket?, updateBuckets: Boolean = true) {
        val select = bucket ?: mBucketMap[BUCKET_ALL] ?: return
        itemChange { _, _ ->
            mBuckets.forEach {
                if (it.id == select.id && !it.select) {
                    it.select = true
                    it.hasPayload()
                } else if (it.id != select.id && it.select) {
                    it.select = false
                    it.hasPayload()
                }
            }
            updateBuckets
        }
        selectBucketLiveData.postValue(select)
        runOnMain {
            imageLiveData.loadRefresh()
        }
    }

    private fun load(): List<Bucket> {

        val data = mApp.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf("_data", "bucket_id", "bucket_display_name", "date_added"),
            "mime_type like '%image/jp%' and _size > 0 ",
            null,
            "date_added DESC"
        )

        val buckets = mutableListOf<Bucket>()
        val bucketMap = mutableMapOf<String, Bucket>()

        //添加全部分类
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
                    //往全部图片中添加
                    bucketAll.images.add(image)

                    if (!bucketId.isNullOrEmpty() && !bucketName.isNullOrEmpty()) {
                        bucketMap[bucketId]?.apply {
                            //图片类别已存在，直接向给分类中添加图片
                            this.images.add(image)
                        } ?: let {
                            //图片类别不存在，创建分类，添加图片，并保存
                            Bucket(bucketId, bucketName).apply {
                                bucketMap[bucketId] = this
                                buckets.add(this)
                                images.add(image)
                            }
                        }
                    }
                }
            }
        }
        data?.close()

        //添加已选择分类
        val bucketSelect = Bucket(BUCKET_SELECT, mApp.getString(R.string.aclin_image_picker_gallery_bucket_select))
        buckets.add(bucketSelect)
        bucketMap[bucketSelect.id] = bucketSelect

        mBuckets = buckets
        mBucketMap = bucketMap

        selectBucket(selectBucketLiveData.value, false)

        return mBuckets
    }
}