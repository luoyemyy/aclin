package com.github.luoyemyy.aclin.image.picker.gallery

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object GalleryBindingAdapter {

    @JvmStatic
    @BindingAdapter("aclin_image_url")
    fun imageUrl(imageView: ImageView, url: String?) {
        Glide.with(imageView).load(url).into(imageView)
    }

    @JvmStatic
    @BindingAdapter("aclin_image_url_inside")
    fun imageUrl2(imageView: ImageView, url: String?) {
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        Glide.with(imageView).load(url).into(imageView)
    }
}