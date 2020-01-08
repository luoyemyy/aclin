package com.github.luoyemyy.aclin.image.picker.gallery

import android.app.Application
import android.provider.MediaStore
import com.github.luoyemyy.aclin.R
import java.io.File

class GalleryModel(private val mApp: Application) {

    companion object {
        const val BUCKET_ALL = "bucketAll"
        const val BUCKET_SELECT = "bucketSelect"
    }

    fun getBuckets(): List<Bucket> {

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

        return buckets
    }
}