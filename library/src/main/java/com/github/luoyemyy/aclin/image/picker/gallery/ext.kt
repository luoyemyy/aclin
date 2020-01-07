package com.github.luoyemyy.aclin.image.picker.gallery

import android.content.Context
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.github.luoyemyy.aclin.ext.dp2px

typealias GalleryCallback = (ArrayList<String>) -> Unit

internal const val BUS_EVENT_SELECT_BUCKET = "bus_event_select_bucket"
internal const val LAST_SELECT_BUCKET = "last_select_bucket"

internal fun calculateImageItemSize(context: Context, suggestDp: Int = 80): Pair<Int, Int> {
    return context.resources.displayMetrics.widthPixels.let {
        val span = (it / context.dp2px(suggestDp))
        Pair(span, it / span)
    }
}

@BindingAdapter("aclin_image_url")
fun imageUrl(imageView: ImageView, url: String?) {
    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    Glide.with(imageView).load(url).into(imageView)
}

@BindingAdapter("aclin_image_url_preview")
fun imageUrl2(imageView: ImageView, url: String?) {
    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
    Glide.with(imageView).load(url).into(imageView)
}

@BindingAdapter("aclin_image_url_crop")
fun imageUrl3(imageView: ImageView, url: String?) {
    imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
    Glide.with(imageView).load(url).into(imageView)
}