package com.xannanov.course.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

class TakePictureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onActionUpListener: () -> Unit = {}

    private var strokeWidth = DEFAULT_STROKE_WIDTH
    private var cx = 0f
    private var cy = 0f
    private var radius = 0f
    private val paint: Paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = this@TakePictureView.strokeWidth
        style = Paint.Style.STROKE
    }

    private val strokeValueAnimator: ValueAnimator = ValueAnimator.ofFloat(DEFAULT_STROKE_WIDTH, DEFAULT_STROKE_WIDTH * 3).apply {
        duration = ANIMATION_DURATION
        interpolator = LinearInterpolator()
        addUpdateListener {
            paint.strokeWidth = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val (width, height) = resolveDim()
        setMeasuredDimension(width, height)
    }

    private fun resolveDim(): Pair<Int, Int> =
        DIMEN + DIMEN / 2 + DEFAULT_STROKE_WIDTH.toInt() to DIMEN + DIMEN / 2 + DEFAULT_STROKE_WIDTH.toInt()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        cx = w.toFloat() / 2f
        cy = h.toFloat() / 2f
        radius = DIMEN.toFloat() / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(cx, cy, radius, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN ->
                actionDown(x, y)
            MotionEvent.ACTION_UP ->
                actionUp()
        }
        return true
    }

    private fun actionUp() {
        stopAnimator()
        onActionUpListener()
    }

    private fun actionDown(x: Float, y: Float) {
        val tempX = x - cx
        val tempY = y - cy
        if (tempX * tempX + tempY * tempY <= radius * radius) {
            runAnimator()
        }
    }

    private fun runAnimator() {
        strokeValueAnimator.start()
    }

    private fun stopAnimator() {
        strokeValueAnimator.reverse()
    }

    companion object {
        private const val DIMEN = 120
        private const val DEFAULT_STROKE_WIDTH = 10f
        private const val ANIMATION_DURATION = 200L
    }
}