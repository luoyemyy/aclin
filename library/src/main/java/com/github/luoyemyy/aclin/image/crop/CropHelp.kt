package com.github.luoyemyy.aclin.image.crop

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.ImageView
import com.github.luoyemyy.aclin.image.preview.PreviewHelper
import kotlin.math.min

class CropHelp(private val mImageView: ImageView) : PreviewHelper(mImageView) {

    private var mMaskRatio = 0.75f
    private var mMaskColor: Int = 0x80000000.toInt()
    private val mPaint = Paint().apply {
        color = mMaskColor
    }
    private val mStrokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    fun setMaskRatio(ratio: Float) {
        mMaskRatio = ratio
        mImageView.invalidate()
    }

    /**
     * 画裁剪区域
     */
    fun drawMask(canvas: Canvas?) {
        val cropRect = calculateCropSpace()
        val leftRect = RectF(0f, cropRect.top, cropRect.left, cropRect.bottom)
        val topRect = RectF(0f, 0f, mImageView.width.toFloat(), cropRect.top)
        val rightRect = RectF(cropRect.right, cropRect.top, mImageView.width.toFloat(), cropRect.bottom)
        val bottomRect = RectF(0f, cropRect.bottom, mImageView.width.toFloat(), mImageView.height.toFloat())

        canvas?.drawRect(leftRect, mPaint)
        canvas?.drawRect(topRect, mPaint)
        canvas?.drawRect(rightRect, mPaint)
        canvas?.drawRect(bottomRect, mPaint)
        canvas?.drawRect(cropRect, mStrokePaint)
    }

    /**
     * 计算裁剪区域
     */
    private fun calculateCropSpace(): RectF {
        val w = mImageView.width * 0.8
        val h = w * mMaskRatio
        val (x, y) = Pair(w.toInt(), min(mImageView.height, h.toInt()))
        return RectF((mImageView.width / 2 - x / 2).toFloat(),
                     (mImageView.height / 2 - y / 2).toFloat(),
                     (mImageView.width / 2 + x / 2).toFloat(),
                     (mImageView.height / 2 + y / 2).toFloat())
    }

}