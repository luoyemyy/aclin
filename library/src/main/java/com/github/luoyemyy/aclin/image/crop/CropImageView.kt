package com.github.luoyemyy.aclin.image.crop

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

class CropImageView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : ImageView(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    private val mHelp: CropHelp = CropHelp(this)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mHelp.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mHelp.drawMask(canvas)
    }

    fun setMaskRatio(ratio: Float) {
        mHelp.setMaskRatio(ratio)
    }
}