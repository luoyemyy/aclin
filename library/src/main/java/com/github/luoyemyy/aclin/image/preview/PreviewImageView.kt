package com.github.luoyemyy.aclin.image.preview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

class PreviewImageView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : ImageView(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    private val mHelper = PreviewHelper(this)

    /**
     * 接管事件
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mHelper.onTouchEvent(event)
    }
}