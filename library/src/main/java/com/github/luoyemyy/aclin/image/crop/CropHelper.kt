package com.github.luoyemyy.aclin.image.crop

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.values
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.runOnThread
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

    override fun getLimitRect(): RectF {
        return calculateCropSpace()
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
        val vw = mImageView.width * 1f
        val vh = mImageView.height * 1f
        val dRatio = vw / vh
        val (w, h) = if (dRatio > mMaskRatio) {
            Pair(vh * mMaskRatio * mMaskPadding, vh * mMaskPadding)
        } else {
            Pair(vw * mMaskPadding, vw / mMaskRatio * mMaskPadding)
        }
        return RectF(vw / 2 - w / 2, vh / 2 - h / 2, vw / 2 + w / 2, vh / 2 + h / 2)
    }

    private fun checkBounds(): Boolean {
        val afterCropRect = calculateCropSpace() //缩放后-裁剪区域坐标
        val bitmap = (mImageView.drawable as? BitmapDrawable)?.bitmap ?: return true
        val afterBitmapRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()).also {
            mImageView.imageMatrix.mapRect(it) //缩放后-图片区域坐标
        }
        return when {
            afterBitmapRect.right - afterBitmapRect.left < afterCropRect.right - afterCropRect.left -> false
            afterBitmapRect.bottom - afterBitmapRect.top < afterCropRect.bottom - afterCropRect.top -> false
            afterBitmapRect.left > afterCropRect.left -> false
            afterBitmapRect.top > afterCropRect.top -> false
            afterBitmapRect.right < afterCropRect.right -> false
            afterBitmapRect.bottom < afterCropRect.bottom -> false
            else -> true
        }
    }

    fun crop(failure: ((Throwable?) -> Unit)? = null, success: (Bitmap) -> Unit) {
        if (!checkBounds()) {
            failure?.invoke(null)
            addAction(LimitAction())
            return
        }
        runOnThread {
            var cropBitmap: Bitmap? = null
            try {
                //变换后-裁剪区域坐标
                val afterCropRect = calculateCropSpace()
                val bitmap = (mImageView.drawable as BitmapDrawable).bitmap
                //变换后-图片区域坐标
                val afterBitmapRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()).also {
                    mImageView.imageMatrix.mapRect(it)
                }

                //变换后-裁剪区域相对于图片坐标
                val cw = afterCropRect.right - afterCropRect.left
                val ch = afterCropRect.bottom - afterCropRect.top
                val cx = afterCropRect.left - afterBitmapRect.left
                val cy = afterCropRect.top - afterBitmapRect.top

                //计算图片的缩放比例
                val scale = mImageView.imageMatrix.values()[Matrix.MSCALE_X]
                //变换前-裁剪区域坐标
                val beforeCropRect = RectF(cx, cy, cx + cw, cy + ch).also {
                    Matrix().apply {
                        postScale(1 / scale, 1 / scale)
                        mapRect(it)
                    }
                }

                //变换前-裁剪区域相对于图片坐标
                val x = beforeCropRect.left.toInt()
                val y = beforeCropRect.top.toInt()
                val w = (beforeCropRect.right - beforeCropRect.left).toInt()
                val h = (beforeCropRect.bottom - beforeCropRect.top).toInt()

//                Log.e("CropHelper", "crop:  图片尺寸 ${bitmap.width} ${bitmap.height}")
//                Log.e("CropHelper", "crop:  变换后-裁剪区域坐标 afterCropRect $afterCropRect")
//                Log.e("CropHelper", "crop:  变换后-图片区域坐标 afterBitmapRect $afterBitmapRect")
//                Log.e("CropHelper", "crop:  变换后-裁剪区域相对于图片坐标 ${RectF(cx, cy, cx + cw, cy + ch)}")
//                Log.e("CropHelper", "crop:  缩放比例 $scale")
//                Log.e("CropHelper", "crop:  变换前-裁剪区域相对于图片坐标 $beforeCropRect")

                val matrix = Matrix().apply {
                    postScale(scale, scale)
                }

                cropBitmap = Bitmap.createBitmap(bitmap, x, y, w, h, matrix, false)
//                Log.e("CropHelper", "crop:  裁剪图片宽度 ${cropBitmap.width} 高度 ${cropBitmap.height}")
            } catch (e: Throwable) {
                Log.e("CropHelper", "crop:  ", e)
                runOnMain {
                    failure?.invoke(e)
                }
            } finally {
                runOnMain {
                    cropBitmap?.apply {
                        mImageView.setImageBitmap(this)
                        success(this)
                    } ?: let { failure?.invoke(null) }
                }
            }
        }
    }
}