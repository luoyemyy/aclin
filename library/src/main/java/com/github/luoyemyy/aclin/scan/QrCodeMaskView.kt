package com.github.luoyemyy.aclin.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class QrCodeMaskView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    private var mMaskColor: Int = 0x80000000.toInt()
    private val mPaint = Paint().apply {
        color = mMaskColor
    }
    private val mStrokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawMask(canvas)
    }

    private fun drawMask(canvas: Canvas?) {
        canvas?.apply {
            val cropRect = calculateMaskSpace()
            val leftRect = RectF(0f, cropRect.top, cropRect.left, cropRect.bottom)
            val topRect = RectF(0f, 0f, width.toFloat(), cropRect.top)
            val rightRect = RectF(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom)
            val bottomRect = RectF(0f, cropRect.bottom, width.toFloat(), height.toFloat())

            canvas.drawRect(leftRect, mPaint)
            canvas.drawRect(topRect, mPaint)
            canvas.drawRect(rightRect, mPaint)
            canvas.drawRect(bottomRect, mPaint)
            canvas.drawRect(cropRect, mStrokePaint)
        }
    }

    private fun calculateMaskSpace(): RectF {
        val vw = width * 1f
        val vh = height * 1f
        val vRatio = vw / vh
        val w: Float
        val h: Float
        if (vRatio > 1) {
            w = vh * QrCodeBuilder.SCAN_PERCENT
            h = vh * QrCodeBuilder.SCAN_PERCENT
        } else {
            w = vw * QrCodeBuilder.SCAN_PERCENT
            h = vw * QrCodeBuilder.SCAN_PERCENT
        }
        return RectF(vw / 2 - w / 2, vh / 2 - h / 2, vw / 2 + w / 2, vh / 2 + h / 2)
    }


}