package com.xannanov.course.views

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Отношение площади в пикселях к площади в сантиметрах (формула)
a = 2px; b = 7px
1px = 17cm
Spx = 2 * 7 = 14px^2
Scm = Spx * 17^2 = 14 * 17^2 = 4046cm^2
 **/

class SelectionCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var mCanvas: Canvas
    private lateinit var mBitmap: Bitmap

    private val mPath: Path = Path()
    private val mPaintStrokeRed: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private var prevX = 0f
    private var prevY = 0f

    private var paintMode: PaintMode = PaintMode.IDLE
    private var paintMethod: PaintMethod = PaintMethod.Finger

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::mBitmap.isInitialized) mBitmap.recycle()

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()

        when (paintMode) {
            PaintMode.IDLE, PaintMode.Painting -> {
                mCanvas.drawPath(mPath, mPaintStrokeRed)

                canvas.drawBitmap(mBitmap, 0f, 0f, null)
            }
            PaintMode.Finishing -> {
                drawBackground()

                canvas.drawBitmap(mBitmap, 0f, 0f, null)
            }
            PaintMode.Finished -> {

            }
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (paintMode != PaintMode.Finished) {

            var x = 0f
            var y = 0f
            when (paintMethod) {
                PaintMethod.Finger -> {
                    x = event.x
                    y = event.y
                }
                PaintMethod.FingerWithOffset -> {
                    x = event.x - DEFAULT_OFFSET
                    y = event.y - DEFAULT_OFFSET
                }
            }

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    touchMove(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    touchUp(x, y)
                    invalidate()
                }
            }
        }
        return paintMode != PaintMode.Finished
    }

    fun clear() {

    }

    private fun touchStart(x: Float, y: Float) {
        paintMode = PaintMode.Painting

        prevX = x
        prevY = y

        mPath.moveTo(x, y)
    }

    private fun touchMove(x: Float, y: Float) {
        mPath.lineTo(x, y)
    }

    private fun touchUp(x: Float, y: Float) {
        if (prevX != x && prevY != y) {
            mPath.lineTo(x, y)
            paintMode = PaintMode.Finishing
        }
    }

    private fun drawBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mCanvas.clipPath(mPath, Region.Op.DIFFERENCE)
        } else {
            mCanvas.clipOutPath(mPath)
        }

        mCanvas.drawColor(Color.argb(180, 0, 0, 0))

        paintMode = PaintMode.Finished
    }

    private fun getPointsOnPath(path: Path): ArrayList<Point> {
        val result = ArrayList<Point>()
        val pm = PathMeasure(path, false)

        var distance = 0f
        val currentCoords = FloatArray(2)

        while (distance < pm.length) {
            pm.getPosTan(distance, currentCoords, null)
            result.add(Point(currentCoords[0].toInt(), currentCoords[1].toInt()))

            distance++
        }

        return result
    }

    public fun calculateAreaInPixels(points: ArrayList<Point> = getPointsOnPath(mPath)): Double {
        // Формула Гаусса
        var tempSum = .0
        for (i in 0 until points.size) {
            tempSum += points[i].x * points[(i + 1) % points.size].y -
                    points[(i + 1) % points.size].x * points[i].y
        }

        return .5 * abs(tempSum)
    }

    companion object {
        private const val DEFAULT_OFFSET = 50
    }
}

sealed class PaintMode {
    object IDLE : PaintMode()
    object Painting : PaintMode()
    object Finishing : PaintMode()
    object Finished : PaintMode()
}

sealed class PaintMethod {
    object Finger : PaintMethod()
    object FingerWithOffset : PaintMethod()
}
