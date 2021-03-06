package com.github.luoyemyy.aclin.image.preview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.graphics.values

open class PreviewHelper(private val mImageView: ImageView) {

    private var mMatrix = Matrix()
    private var mResetMatrix = Matrix()
    private val mScaleGestureDetector = ScaleGestureDetector(mImageView.context, ScaleGestureListener())
    private val mGestureDetector = GestureDetector(mImageView.context, GestureListener())
    private var mVWidth: Int = 0
    private var mVHeight: Int = 0
    private var mLastScaleX: Float = 0f
    private var mLastScaleY: Float = 0f
    private val mAnimDuration = 240L
    private val mMaxScale = 5f
    private val mMinScale = 1f
    private var mCurrentAction: Action? = null
    private var mIsPreview = false

    init {
        mImageView.viewTreeObserver.addOnGlobalLayoutListener {
            if (mVWidth == 0 && mVHeight == 0) {
                mVWidth = mImageView.width
                mVHeight = mImageView.height
            }
        }
    }

    private fun setMatrixType() {
        val dWidth = mImageView.drawable?.intrinsicWidth ?: 0
        val dHeight = mImageView.drawable?.intrinsicHeight ?: 0
        if (dWidth > 0 && dHeight > 0 && mImageView.scaleType != ImageView.ScaleType.MATRIX) {
            mMatrix.set(mImageView.imageMatrix)
            mResetMatrix.set(mImageView.imageMatrix)
            mImageView.scaleType = ImageView.ScaleType.MATRIX
            mImageView.imageMatrix = mMatrix
        }
    }

    private fun equalsMatrix(matrix1: Matrix, matrix2: Matrix): Boolean {
        val array1 = matrix1.values()
        val array2 = matrix2.values()
        return (0..8).all { array1[it] == array2[it] }
    }

    protected fun addAction(action: Action) {
        mCurrentAction?.apply {
            if (action.canRun(this)) {
                if (isRunning) {
                    cancel()
                }
                mCurrentAction = action
                action.start()
            }
        } ?: let {
            mCurrentAction = action
            action.start()
        }
    }

    open fun nestScroll(): Boolean {
        return true
    }

    /**
     * 接管事件
     */
    fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        if (!nestScroll() || event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_DOWN) {
            mIsPreview = true
        }
        mImageView.parent.requestDisallowInterceptTouchEvent(mIsPreview)
        mGestureDetector.onTouchEvent(event)
        mScaleGestureDetector.onTouchEvent(event)
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP && mCurrentAction !is FlingAction) {
            addAction(LimitAction())
        }
        return true
    }

    /**
     * 图片缩放
     */
    private fun scale(scale: Float, x: Float, y: Float) {
        mMatrix.postScale(scale, scale, x, y)
        mImageView.imageMatrix = mMatrix
    }

    /**
     * 图片平移
     */
    private fun translate(x: Float, y: Float) {
        mMatrix.postTranslate(x, y)
        mImageView.imageMatrix = mMatrix
    }

    private fun animatorScale(endScale: Float, x: Float, y: Float): ValueAnimator {
        return ValueAnimator().apply {
            val startScale = mMatrix.values()[Matrix.MSCALE_X]
            var preScale = 1f
            setObjectValues(startScale, endScale)
            interpolator = DecelerateInterpolator()
            duration = mAnimDuration
            addUpdateListener {
                (it.animatedValue as Float).apply {
                    scale(this / preScale, x, y)
                    preScale = this
                }
            }
            start()
        }
    }

    /**
     * 动画
     */
    private fun animator(startMatrix: Matrix, endMatrix: Matrix): ValueAnimator {
        return ValueAnimator().apply {
            val start = startMatrix.values()
            val end = endMatrix.values()
            setObjectValues(start, end)
            interpolator = DecelerateInterpolator()
            duration = mAnimDuration.toLong()
            setEvaluator { fraction, _, _ ->
                val v = FloatArray(9)
                for (i in (0..8)) {
                    v[i] = start[i] + (end[i] - start[i]) * fraction
                }
                Matrix().apply { setValues(v) }
            }
            addUpdateListener {
                mMatrix.set(it.animatedValue as Matrix)
                mImageView.imageMatrix = mMatrix
            }
            start()
        }
    }

    protected fun getDrawableRect(): RectF? {
        val dWidth = mImageView.drawable?.intrinsicWidth ?: 0
        val dHeight = mImageView.drawable?.intrinsicHeight ?: 0
        if (dWidth == 0 || dHeight == 0) return null
        return RectF(0f, 0f, dWidth.toFloat(), dHeight.toFloat())
    }

    private fun getBitmapRect(matrix: Matrix): RectF? {
        return getDrawableRect()?.let {
            matrix.mapRect(it)
            it
        }
    }

    internal open fun getLimitRect(): RectF {
        return RectF(0f, 0f, mVWidth.toFloat(), mVHeight.toFloat())
    }

    /**
     * 限制
     */
    private fun calculateLimitMatrix(): Matrix? {
        val endMatrix = Matrix(mMatrix)

        //scale
        val currentScale = mMatrix.values()[Matrix.MSCALE_X]
        val dScale = when {
            currentScale > mMaxScale -> mMaxScale / currentScale
            currentScale < mMinScale -> mMinScale / currentScale
            else -> 0f
        }
        if (dScale > 0) {
            endMatrix.postScale(dScale, dScale, mLastScaleX, mLastScaleY)
        }

        val bitmapRect = getBitmapRect(endMatrix) ?: return null
        val limitRect = getLimitRect()

        //scroll
        val (x, y) = calculateScroll(bitmapRect, limitRect)
        endMatrix.postTranslate(x, y)
        return endMatrix
    }

    private fun calculateScroll(bitmapRect: RectF, limitRect: RectF): Pair<Float, Float> {
        val x = when {
            //bitmap 宽度 小于 限制宽度
            bitmapRect.right - bitmapRect.left <= limitRect.right - limitRect.left -> when {
                //bitmap 左边 小于 限制左边 ，向右移差值
                bitmapRect.left < limitRect.left -> limitRect.left - bitmapRect.left
                //bitmap 右边 大于 限制右边 ，向左移差值
                bitmapRect.right > limitRect.right -> limitRect.right - bitmapRect.right
                else -> 0f
            }
            //bitmap 宽度 大于 限制宽度
            bitmapRect.right - bitmapRect.left > limitRect.right - limitRect.left -> when {
                //bitmap 左边 大于 限制左边 ，向左移差值
                bitmapRect.left > limitRect.left -> limitRect.left - bitmapRect.left
                //bitmap 右边 小于 限制右边 ，向右移差值
                bitmapRect.right < limitRect.right -> limitRect.right - bitmapRect.right
                else -> 0f
            }
            else -> 0f
        }

        val y = when {
            //bitmap 高度度 小于 限制高度
            bitmapRect.bottom - bitmapRect.top <= limitRect.bottom - limitRect.top -> when {
                //bitmap 上边 小于 限制上边 ，向下移差值
                bitmapRect.top < limitRect.top -> limitRect.top - bitmapRect.top
                //bitmap 下边 大于 限制下边 ，向上移差值
                bitmapRect.bottom > limitRect.bottom -> limitRect.bottom - bitmapRect.bottom
                else -> 0f
            }
            //bitmap 高度 大于 限制高度
            bitmapRect.bottom - bitmapRect.top > limitRect.bottom - limitRect.top -> when {
                //bitmap 上边 大于 限制上边 ，向上移差值
                bitmapRect.top > limitRect.top -> limitRect.top - bitmapRect.top
                //bitmap 下边 小于 限制下边 ，向下移差值
                bitmapRect.bottom < limitRect.bottom -> limitRect.bottom - bitmapRect.bottom
                else -> 0f
            }
            else -> 0f
        }
        return Pair(x, y)
    }

    inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mLastScaleX = detector.focusX
            mLastScaleY = detector.focusY
            addAction(ScaleAction(detector.scaleFactor, detector.focusX, detector.focusY))
            return true
        }
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            setMatrixType()
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (mIsPreview) {
                addAction(ScrollAction(-distanceX, -distanceY))
            }
            return mIsPreview
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (mMatrix.values().contentEquals(mResetMatrix.values())) {
                addAction(ScaleMaxAction(e.x, e.y))
            } else {
                addAction(ResetAction())
            }
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (mIsPreview) {
                addAction(FlingAction(velocityX, velocityY))
            }
            return mIsPreview
        }

    }

    inner class ScrollAction(private val dx: Float, private val dy: Float) : Action(1) {
        override fun start() {
            isRunning = true
            translate(dx, dy)
            isRunning = false
        }
    }

    inner class ScaleAction(private val scale: Float, private val x: Float, private val y: Float) : Action(1) {
        override fun start() {
            mIsPreview = true
            isRunning = true
            scale(scale, x, y)
            isRunning = false
        }
    }

    inner class ScaleMaxAction(private val x: Float, private val y: Float) : Action(1) {

        private lateinit var mAnimator: ValueAnimator

        override fun start() {
            mIsPreview = true
            mAnimator = animatorScale(mMaxScale, x, y).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        this@ScaleMaxAction.isRunning = false
                    }
                })
            }
            isRunning = true
        }

        override fun cancel() {
            if (isRunning && this::mAnimator.isInitialized) {
                mAnimator.cancel()
                isRunning = false
            }
        }
    }

    inner class ResetAction : Action(2) {

        private lateinit var mAnimator: ValueAnimator

        override fun start() {
            mAnimator = animator(mMatrix, mResetMatrix).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        this@ResetAction.isRunning = false
                        if (nestScroll()) {
                            mIsPreview = false
                        }
                    }
                })
            }
            isRunning = true
        }

        override fun cancel() {
            if (isRunning && this::mAnimator.isInitialized) {
                mAnimator.cancel()
                isRunning = false
            }
        }
    }

    inner class FlingAction(velocityX: Float, velocityY: Float) : Action(3) {
        private lateinit var mAnimator: ValueAnimator
        private var prev = 0.0f
        private var prevVelocityX = velocityX / 1000
        private var prevVelocityY = velocityY / 1000
        private val ax = prevVelocityX / mAnimDuration.toFloat()
        private val ay = prevVelocityY / mAnimDuration.toFloat()
        override fun start() {
            mAnimator = ObjectAnimator.ofFloat(0f, mAnimDuration.toFloat()).apply {
                interpolator = DecelerateInterpolator()
                duration = mAnimDuration.toLong()

                addUpdateListener {
                    val time = it.animatedValue as Float
                    val dt = time - prev
                    val dx = prevVelocityX * dt
                    val dy = prevVelocityY * dt

                    translate(dx, dy)
                    prevVelocityX -= ax * dt
                    prevVelocityY -= ay * dt
                    prev = time
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        this@FlingAction.isRunning = false
                        addAction(LimitAction())
                    }
                })
                start()
            }
            isRunning = true
        }

        override fun cancel() {
            if (isRunning && this::mAnimator.isInitialized) {
                mAnimator.cancel()
                isRunning = false
            }
        }

    }

    inner class LimitAction : Action(3) {
        private lateinit var mAnimator: ValueAnimator
        override fun start() {
            val endMatrix = calculateLimitMatrix() ?: return
            if (!equalsMatrix(mMatrix, endMatrix)) {
                mAnimator = animator(mMatrix, endMatrix).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            this@LimitAction.isRunning = false
                        }
                    })
                }
                isRunning = true
            }
        }

        override fun cancel() {
            if (isRunning && this::mAnimator.isInitialized) {
                mAnimator.cancel()
                isRunning = false
            }
        }

    }

    abstract inner class Action(private val priority: Int) {

        var isRunning: Boolean = false

        fun canRun(prevAction: Action) = !prevAction.isRunning || priority <= prevAction.priority

        abstract fun start()

        open fun cancel() {}
    }


}