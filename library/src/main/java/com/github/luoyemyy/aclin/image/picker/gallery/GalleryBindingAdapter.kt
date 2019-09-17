package com.github.luoyemyy.aclin.image.picker.gallery

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object GalleryBindingAdapter {

    @JvmStatic
    @BindingAdapter("aclin_image_url")
    fun imageUrl(imageView: ImageView, url: String?) {
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(imageView).load(url).into(imageView)
    }

    @JvmStatic
    @BindingAdapter("aclin_image_url_preview")
    fun imageUrl2(imageView: ImageView, url: String?) {
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        Glide.with(imageView).load(url).into(imageView)
    }

    @JvmStatic
    @BindingAdapter("aclin_image_url_crop")
    fun imageUrl3(imageView: ImageView, url: String?) {
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        Glide.with(imageView).load(url).into(imageView)
    }
}