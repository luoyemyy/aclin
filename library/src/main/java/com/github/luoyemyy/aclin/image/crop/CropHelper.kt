package com.github.luoyemyy.aclin.image.crop

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.ImageView
import com.github.luoyemyy.aclin.image.preview.PreviewHelper

class CropHelper(private val mImageView: ImageView) : PreviewHelper(mImageView) {

    private var mMaskRatio = 1f
    private var mMaskPadding = 0.9f
    private var mMaskColor: Int = 0x80000000.toInt()
    private val mPaint = Paint().apply {
        color = mMaskColor
    }
    private val mStrokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    override fun nestScroll(): Boolean {
        return false
    }

    fun setMaskRatio(ratio: Float) {
        mMaskRatio = ratio
        mImageView.invalidate()
    }

    /**
     * 画裁剪区域
     */
    fun drawMask(canvas: Canvas?) {
        val cropRect = calculateCropSpace() ?: return
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
    private fun calculateCropSpace(): RectF? {
        val vw = mImageView.width * 1f
        val vh = mImageView.height * 1f
        val dr = if (mImageView.isInEditMode) RectF(0f, 0f, vw, vh) else getDrawableRect() ?: return null
        val dw = dr.right
        val dh = dr.bottom
        val dRatio = dw / dh
        val (w, h) = if (dRatio > mMaskRatio) {
            Pair(dh * mMaskRatio * mMaskPadding, dh * mMaskPadding)
        } else {
            Pair(dw * mMaskPadding, dw / mMaskRatio * mMaskPadding)
        }
        return RectF(vw / 2 - w / 2, vh / 2 - h / 2, vw / 2 + w / 2, vh / 2 + h / 2)
    }

}