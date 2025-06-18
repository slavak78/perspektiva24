package ru.crmkurgan.main.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import ru.crmkurgan.main.R
import ru.crmkurgan.main.utils.ScanUtils.compareFloats
import kotlin.math.abs

class TouchImageView : AppCompatImageView {
    var normalizedScale = 0f
    var thisMatrix: Matrix? = null
    private var prevMatrix: Matrix? = null

    enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    var state1: State? = null
    var minScale = 0f
    var maxScale = 0f
    private var superMinScale = 0f
    private var superMaxScale = 0f
    var m: FloatArray? = null
    private var context1: Context? = null
    var fling: Fling? = null
    private var mScaleType: ScaleType? = null
    private var imageRenderedAtLeastOnce = false
    private var onDrawReady = false
    private var delayedZoomVariables: ZoomVariables? = null
    var viewWidth = 0
    var viewHeight = 0
    private var prevViewWidth = 0
    private var prevViewHeight = 0
    private var matchViewWidth = 0f
    private var matchViewHeight = 0f
    private var prevMatchViewWidth = 0f
    private var prevMatchViewHeight = 0f
    var mScaleDetector: ScaleGestureDetector? = null
    var mGestureDetector: GestureDetector? = null
    var userTouchListener: OnTouchListener? = null

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        sharedConstructing(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        this.context1 = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mGestureDetector = GestureDetector(context, GestureListener())
        thisMatrix = Matrix()
        val mt = thisMatrix!!
        prevMatrix = Matrix()
        m = FloatArray(9)
        normalizedScale = 1f
        val sT = mScaleType
        if (sT == null) {
            mScaleType = ScaleType.FIT_CENTER
        }
        minScale = 1f
        maxScale = 3f
        superMinScale = SUPER_MIN_MULTIPLIER * 1
        superMaxScale = SUPER_MAX_MULTIPLIER * 3
        imageMatrix = mt
        scaleType = ScaleType.MATRIX
        state1 = State.NONE
        onDrawReady = false
        super.setOnTouchListener(PrivateOnTouchListener())
    }

    override fun setOnTouchListener(l: OnTouchListener) {
        userTouchListener = l
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setScaleType(type: ScaleType) {
        if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
            throw UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END")
        }
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX)
        } else {
            mScaleType = type
            if (onDrawReady) {
                setZoom(this)
            }
        }
    }

    override fun getScaleType(): ScaleType {
        return mScaleType!!
    }

    private val isZoomed: Boolean
        get() = !compareFloats(normalizedScale.toDouble(), 1.0)

    private fun savePreviousImageValues() {
        if (thisMatrix != null && viewHeight != 0 && viewWidth != 0) {
            val m1 = m
            thisMatrix!!.getValues(m1)
            if (prevMatrix != null) {
                prevMatrix!!.setValues(m1)
            }
            prevMatchViewHeight = matchViewHeight
            prevMatchViewWidth = matchViewWidth
            prevViewHeight = viewHeight
            prevViewWidth = viewWidth
        }
    }

    public override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putFloat("saveScale", normalizedScale)
        bundle.putFloat("matchViewHeight", matchViewHeight)
        bundle.putFloat("matchViewWidth", matchViewWidth)
        bundle.putInt("viewWidth", viewWidth)
        bundle.putInt("viewHeight", viewHeight)
        val m1 = m
        val mt = thisMatrix
        mt?.getValues(m1)
        bundle.putFloatArray("matrix", m1)
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce)
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            normalizedScale = state.getFloat("saveScale")
            m = state.getFloatArray("matrix")
            val m1 = m
            if (prevMatrix != null) {
                prevMatrix!!.setValues(m1)
            }
            prevMatchViewHeight = state.getFloat("matchViewHeight")
            prevMatchViewWidth = state.getFloat("matchViewWidth")
            prevViewHeight = state.getInt("viewHeight")
            prevViewWidth = state.getInt("viewWidth")
            imageRenderedAtLeastOnce = state.getBoolean("imageRendered")
            super.onRestoreInstanceState(state.getParcelable("instanceState"))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onDraw(canvas: Canvas) {
        onDrawReady = true
        imageRenderedAtLeastOnce = true
        if (delayedZoomVariables != null) {
            setZoom(
                delayedZoomVariables!!.scale,
                delayedZoomVariables!!.focusX,
                delayedZoomVariables!!.focusY,
                delayedZoomVariables!!.scaleType
            )
            delayedZoomVariables = null
        }
        super.onDraw(canvas)
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        savePreviousImageValues()
    }

    fun resetZoom() {
        normalizedScale = 1f
        fitImageToView()
    }

    private fun setZoom(scale: Float, focusX: Float, focusY: Float, scaleType: ScaleType?) {
        if (!onDrawReady) {
            delayedZoomVariables = ZoomVariables(scale, focusX, focusY, scaleType)
            return
        }
        if (scaleType != mScaleType) {
            setScaleType(scaleType!!)
        }
        resetZoom()
        val vW = viewWidth
        val vH = viewHeight
        scaleImage(scale.toDouble(), vW.toFloat() / 2, vH.toFloat() / 2, true)
        val m1 = m
        val mt = thisMatrix
        mt?.getValues(m1)
        m!![Matrix.MTRANS_X] = -(focusX * imageWidth - viewWidth * 0.5f)
        m!![Matrix.MTRANS_Y] = -(focusY * imageHeight - viewHeight * 0.5f)
        thisMatrix!!.setValues(m)
        fixTrans()
        imageMatrix = thisMatrix
    }

    private fun setZoom(img: TouchImageView) {
        val center = img.scrollPosition
        center?.let {
            setZoom(img.normalizedScale, it.x, it.y, img.mScaleType)
        }
    }

    private val scrollPosition: PointF?
        get() {
            val drawable = drawable ?: return null
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            val point =
                transformCoordTouchToBitmap(viewWidth.toFloat() / 2, viewHeight.toFloat() / 2, true)
            point.x /= drawableWidth.toFloat()
            point.y /= drawableHeight.toFloat()
            return point
        }

    fun fixTrans() {
        if (thisMatrix != null) {
            thisMatrix!!.getValues(m)
            val transX = m!![Matrix.MTRANS_X]
            val transY = m!![Matrix.MTRANS_Y]
            val fixTransX = getFixTrans(transX, viewWidth.toFloat(), imageWidth)
            val fixTransY = getFixTrans(transY, viewHeight.toFloat(), imageHeight)
            if (!compareFloats(fixTransX.toDouble(), 0.0) || !compareFloats(
                    fixTransY.toDouble(),
                    0.0
                )
            ) {
                thisMatrix!!.postTranslate(fixTransX, fixTransY)
            }
        }
    }

    fun fixScaleTrans() {
        fixTrans()
        if (thisMatrix != null) {
            thisMatrix!!.getValues(m)
            if (imageWidth < viewWidth) {
                m!![Matrix.MTRANS_X] = (viewWidth - imageWidth) / 2
            }
            if (imageHeight < viewHeight) {
                m!![Matrix.MTRANS_Y] = (viewHeight - imageHeight) / 2
            }
            thisMatrix!!.setValues(m)
        }
    }

    val imageWidth: Float
        get() = matchViewWidth * normalizedScale
    val imageHeight: Float
        get() = matchViewHeight * normalizedScale

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = drawable
        val drawableWidth = drawable?.intrinsicWidth ?: 0
        val drawableHeight = drawable?.intrinsicHeight ?: 0
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        viewWidth = setViewSize(widthMode, widthSize, drawableWidth)
        viewHeight = setViewSize(heightMode, heightSize, drawableHeight)
        setMeasuredDimension(viewWidth, viewHeight)
        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return
        }
        if (thisMatrix == null || prevMatrix == null) {
            return
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        var scaleX = viewWidth.toFloat() / drawableWidth
        var scaleY = viewHeight.toFloat() / drawableHeight
        if (mScaleType != null) {
            when (mScaleType) {
                ScaleType.CENTER -> {
                    scaleY = 1f
                    scaleX = scaleY
                }
                ScaleType.CENTER_CROP -> {
                    scaleY = scaleX.coerceAtLeast(scaleY)
                    scaleX = scaleY
                }
                ScaleType.CENTER_INSIDE -> {
                    scaleY = 1f.coerceAtMost(scaleX.coerceAtMost(scaleY))
                    scaleX = scaleY
                }
                ScaleType.FIT_CENTER -> {
                    scaleY = scaleX.coerceAtMost(scaleY)
                    scaleX = scaleY
                }
                ScaleType.FIT_XY -> {
                }
                else -> throw UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END")
            }
        }
        val redundantXSpace = viewWidth - scaleX * drawableWidth
        val redundantYSpace = viewHeight - scaleY * drawableHeight
        matchViewWidth = viewWidth - redundantXSpace
        matchViewHeight = viewHeight - redundantYSpace
        if (!isZoomed && !imageRenderedAtLeastOnce) {
            thisMatrix!!.setScale(scaleX, scaleY)
            thisMatrix!!.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            normalizedScale = 1f
        } else {
            if (compareFloats(prevMatchViewWidth.toDouble(), 0.0) || compareFloats(
                    prevMatchViewHeight.toDouble(),
                    0.0
                )
            ) {
                savePreviousImageValues()
            }
            prevMatrix!!.getValues(m)
            m!![Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalizedScale
            m!![Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalizedScale
            val transX = m!![Matrix.MTRANS_X]
            val transY = m!![Matrix.MTRANS_Y]
            val prevActualWidth = prevMatchViewWidth * normalizedScale
            val actualWidth = imageWidth
            translateMatrixAfterRotate(
                Matrix.MTRANS_X,
                transX,
                prevActualWidth,
                actualWidth,
                prevViewWidth,
                viewWidth,
                drawableWidth
            )
            val prevActualHeight = prevMatchViewHeight * normalizedScale
            val actualHeight = imageHeight
            translateMatrixAfterRotate(
                Matrix.MTRANS_Y,
                transY,
                prevActualHeight,
                actualHeight,
                prevViewHeight,
                viewHeight,
                drawableHeight
            )
            thisMatrix!!.setValues(m)
        }
        fixTrans()
        imageMatrix = thisMatrix
    }

    private fun translateMatrixAfterRotate(
        axis: Int,
        trans: Float,
        prevImageSize: Float,
        imageSize: Float,
        prevViewSize: Int,
        viewSize: Int,
        drawableSize: Int
    ) {
        if (imageSize < viewSize) {
            m!![axis] = (viewSize - drawableSize * m!![Matrix.MSCALE_X]) * 0.5f
        } else if (trans > 0) {
            m!![axis] = -((imageSize - viewSize) * 0.5f)
        } else {
            val percentage = (abs(trans) + 0.5f * prevViewSize) / prevImageSize
            m!![axis] = -(percentage * imageSize - viewSize * 0.5f)
        }
    }

    fun setState(state: State?) {
        state1 = state
    }

    override fun canScrollVertically(direction: Int): Boolean {
        if (thisMatrix != null) {
            thisMatrix!!.getValues(m)
        }
        val y = m!![Matrix.MTRANS_Y]
        if (imageHeight < viewHeight) {
            return false
        }
        return if (y >= -1 && direction < 0) {
            false
        } else abs(y) + viewHeight + 1 < imageHeight || direction <= 0
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        if (thisMatrix != null) {
            thisMatrix!!.getValues(m)
        }
        val x = m!![Matrix.MTRANS_X]
        if (imageWidth < viewWidth) {
            return false
        }
        return if (x >= -1 && direction < 0) {
            false
        } else abs(x) + viewWidth + 1 < imageWidth || direction <= 0
    }

    @Suppress("DEPRECATION")
    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (context != null) {
                val activity = context as AppCompatActivity
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
                if (toolbar != null) {
                    if (toolbar.visibility == View.VISIBLE) {
                        val flag = (SYSTEM_UI_FLAG_FULLSCREEN
                                or SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                        activity.window.decorView.systemUiVisibility = flag
                        toolbar.visibility = View.GONE
                    } else {
                        val flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                        activity.window.decorView.systemUiVisibility = flag
                        toolbar.visibility = View.VISIBLE
                    }
                }
            }
            return performClick()
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (fling != null) {
                fling!!.cancelFling()
            }
            fling = Fling(velocityX.toInt(), velocityY.toInt())
            compatPostOnAnimation(fling)
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            var consumed = false
            if (state1 == State.NONE) {
                val targetZoom = if (compareFloats(
                        normalizedScale.toDouble(),
                        minScale.toDouble()
                    )
                ) maxScale
                else minScale
                val doubleTap = DoubleTapZoom(targetZoom, e.x, e.y, false)
                compatPostOnAnimation(doubleTap)
                consumed = true
            }
            return consumed
        }
    }

    private inner class PrivateOnTouchListener : OnTouchListener {
        private val last = PointF()

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (mScaleDetector != null) {
                mScaleDetector!!.onTouchEvent(event)
            }
            if (mGestureDetector != null) {
                mGestureDetector!!.onTouchEvent(event)
            }
            val curr = PointF(event.x, event.y)
            if (state1 == State.NONE || state1 == State.DRAG || state1 == State.FLING) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        last.set(curr)
                        if (fling != null) {
                            fling!!.cancelFling()
                        }
                        setState(State.DRAG)
                    }
                    MotionEvent.ACTION_MOVE -> if (state1 == State.DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
                        val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), imageWidth)
                        val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), imageHeight)
                        if (thisMatrix != null) {
                            thisMatrix!!.postTranslate(fixTransX, fixTransY)
                        }
                        fixTrans()
                        last[curr.x] = curr.y
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> setState(State.NONE)
                    else -> {
                    }
                }
            }
            imageMatrix = thisMatrix
            if (userTouchListener != null) {
                userTouchListener!!.onTouch(v, event)
            }
            return true
        }
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            setState(State.ZOOM)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(detector.scaleFactor.toDouble(), detector.focusX, detector.focusY, true)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            setState(State.NONE)
            var animateToZoomBoundary = false
            var targetZoom = normalizedScale
            if (normalizedScale > maxScale) {
                targetZoom = maxScale
                animateToZoomBoundary = true
            } else if (normalizedScale < minScale) {
                targetZoom = minScale
                animateToZoomBoundary = true
            }
            if (animateToZoomBoundary) {
                val doubleTap = DoubleTapZoom(targetZoom, viewWidth / 2f, viewHeight / 2f, true)
                compatPostOnAnimation(doubleTap)
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    fun scaleImage(deltaScale: Double, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) {
        var deltaScale = deltaScale
        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper) {
            lowerScale = superMinScale
            upperScale = superMaxScale
        } else {
            lowerScale = minScale
            upperScale = maxScale
        }
        val origScale = normalizedScale
        normalizedScale *= deltaScale.toFloat()
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale
            deltaScale = (upperScale / origScale).toDouble()
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale
            deltaScale = (lowerScale / origScale).toDouble()
        }
        if (thisMatrix != null) {
            thisMatrix!!.postScale(deltaScale.toFloat(), deltaScale.toFloat(), focusX, focusY)
        }
        fixScaleTrans()
    }

    private inner class DoubleTapZoom(
        targetZoom: Float,
        focusX: Float,
        focusY: Float,
        stretchImageToSuper: Boolean
    ) : Runnable {
        private val startTime: Long
        private val startZoom: Float
        private val targetZoom: Float
        private val bitmapX: Float
        private val bitmapY: Float
        private val stretchImageToSuper: Boolean
        private val interpolator = AccelerateDecelerateInterpolator()
        private val startTouch: PointF?
        private val endTouch: PointF?
        override fun run() {
            val t = interpolate()
            val deltaScale = calculateDeltaScale(t)
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
            fixScaleTrans()
            imageMatrix = thisMatrix
            if (t < 1f) {
                compatPostOnAnimation(this)
            } else {
                setState(State.NONE)
            }
        }

        private fun translateImageToCenterTouchPosition(t: Float) {
            if (startTouch != null && endTouch != null && thisMatrix != null) {
                val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
                val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
                val curr = transformCoordBitmapToTouch(bitmapX, bitmapY)
                thisMatrix!!.postTranslate(targetX - curr.x, targetY - curr.y)
            }
        }

        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime) / 500f
            elapsed = 1f.coerceAtMost(elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom + t * (targetZoom - startZoom)).toDouble()
            return zoom / normalizedScale
        }

        init {
            setState(State.ANIMATE_ZOOM)
            startTime = System.currentTimeMillis()
            startZoom = normalizedScale
            this.targetZoom = targetZoom
            this.stretchImageToSuper = stretchImageToSuper
            val bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false)
            bitmapX = bitmapPoint.x
            bitmapY = bitmapPoint.y
            //
            // Used for translating image during scaling
            //
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF(viewWidth / 2f, viewHeight / 2f)
        }
    }

    fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        if (thisMatrix != null) {
            thisMatrix!!.getValues(m)
        }
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val transX = m!![Matrix.MTRANS_X]
        val transY = m!![Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / imageWidth
        var finalY = (y - transY) * origH / imageHeight
        if (clipToBitmap) {
            finalX = finalX.coerceAtLeast(0f).coerceAtMost(origW)
            finalY = finalY.coerceAtLeast(0f).coerceAtMost(origH)
        }
        return PointF(finalX, finalY)
    }

    fun transformCoordBitmapToTouch(bx: Float, by: Float): PointF {
        if (thisMatrix != null) {
            thisMatrix!!.getValues(m)
        }
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val px = bx / origW
        val py = by / origH
        val finalX = m!![Matrix.MTRANS_X] + imageWidth * px
        val finalY = m!![Matrix.MTRANS_Y] + imageHeight * py
        return PointF(finalX, finalY)
    }

    inner class Fling internal constructor(velocityX: Int, velocityY: Int) : Runnable {
        private var scroller: CompatScroller?
        private var currX: Int
        private var currY: Int
        fun cancelFling() {
            if (scroller != null) {
                setState(State.NONE)
                scroller!!.forceFinished(true)
            }
        }

        override fun run() {
            if (scroller!!.isFinished) {
                scroller = null
                return
            }
            if (scroller!!.computeScrollOffset()) {
                val newX = scroller!!.currX
                val newY = scroller!!.currY
                val transX = newX - currX
                val transY = newY - currY
                currX = newX
                currY = newY
                if (thisMatrix != null) {
                    thisMatrix!!.postTranslate(transX.toFloat(), transY.toFloat())
                }
                fixTrans()
                imageMatrix = thisMatrix
                compatPostOnAnimation(this)
            }
        }

        init {
            setState(State.FLING)
            scroller = CompatScroller(context)
            if (thisMatrix != null) {
                thisMatrix!!.getValues(m)
            }
            val startX = m!![Matrix.MTRANS_X].toInt()
            val startY = m!![Matrix.MTRANS_Y].toInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int
            if (imageWidth > viewWidth) {
                minX = viewWidth - imageWidth.toInt()
                maxX = 0
            } else {
                maxX = startX
                minX = maxX
            }
            if (imageHeight > viewHeight) {
                minY = viewHeight - imageHeight.toInt()
                maxY = 0
            } else {
                maxY = startY
                minY = maxY
            }
            scroller!!.fling(
                startX, startY, velocityX, velocityY, minX,
                maxX, minY, maxY
            )
            currX = startX
            currY = startY
        }
    }

    class CompatScroller internal constructor(context: Context?) {
        private val overScroller: OverScroller = OverScroller(context)
        fun fling(
            startX: Int,
            startY: Int,
            velocityX: Int,
            velocityY: Int,
            minX: Int,
            maxX: Int,
            minY: Int,
            maxY: Int
        ) {
            overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        }

        fun forceFinished(finished: Boolean) {
            overScroller.forceFinished(finished)
        }

        val isFinished: Boolean
            get() = overScroller.isFinished

        fun computeScrollOffset(): Boolean {
            overScroller.computeScrollOffset()
            return overScroller.computeScrollOffset()
        }

        val currX: Int
            get() = overScroller.currX
        val currY: Int
            get() = overScroller.currY
    }

    fun compatPostOnAnimation(runnable: Runnable?) {
        postOnAnimation(runnable)
    }

    private class ZoomVariables(
        val scale: Float,
        val focusX: Float,
        val focusY: Float,
        val scaleType: ScaleType?
    )

    companion object {
        private const val SUPER_MIN_MULTIPLIER = .75f
        private const val SUPER_MAX_MULTIPLIER = 1.25f
        private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
            val minTrans: Float
            val maxTrans: Float
            if (contentSize <= viewSize) {
                minTrans = 0f
                maxTrans = viewSize - contentSize
            } else {
                minTrans = viewSize - contentSize
                maxTrans = 0f
            }
            if (trans < minTrans) {
                return -trans + minTrans
            }
            return if (trans > maxTrans) {
                -trans + maxTrans
            } else 0f
        }

        fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
            return if (contentSize <= viewSize) {
                0f
            } else delta
        }

        private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
            val viewSize: Int = when (mode) {
                MeasureSpec.AT_MOST -> drawableWidth.coerceAtMost(size)
                MeasureSpec.UNSPECIFIED -> drawableWidth
                else -> size
            }
            return viewSize
        }
    }
}