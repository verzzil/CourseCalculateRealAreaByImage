package com.xannanov.course.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.xannanov.course.utils.intersectWithPoint

class CardSelectionCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var mCanvas: Canvas
    private lateinit var mBitmap: Bitmap

    private val selectionRect: Rect = Rect()
    private val topLeftRect: Rect = Rect()
    private val topCenterRect: Rect = Rect()
    private val topRightRect: Rect = Rect()
    private val bottomLeftRect: Rect = Rect()
    private val bottomCenterRect: Rect = Rect()
    private val bottomRightRect: Rect = Rect()
    private val leftEdgeRect: Rect = Rect()
    private val rightEdgeRect: Rect = Rect()

    private val paintRect: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val paintStrokeRect: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
        pathEffect = DashPathEffect(floatArrayOf(40f, 20f), 0f)
    }
    private val paintChangeSizeRect: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private var topLeftX = 0
    private var topLeftY = 0

    private var selectionMode: SelectionMode = SelectionMode.Waiting

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::mBitmap.isInitialized) mBitmap.recycle()

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()

        drawSelectionRectangle()

        canvas.drawBitmap(mBitmap, 0f, 0f, null)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x.toInt()
        val y = event.y.toInt()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                when (selectionMode) {
                    SelectionMode.Waiting ->
                        touchDownInSelectionMode(x, y)
                    SelectionMode.Selection -> {}
                    is SelectionMode.ResizeSelection ->
                        touchDownInResizeMode(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (selectionMode) {
                    SelectionMode.Waiting -> {}
                    SelectionMode.Selection -> {
                        touchMoveInSelectionMode(x, y)
                        invalidate()
                    }
                    is SelectionMode.ResizeSelection -> {
                        touchMoveInResizeMode(x, y)
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                when (selectionMode) {
                    SelectionMode.Waiting -> {}
                    SelectionMode.Selection -> {
                        selectionMode = SelectionMode.ResizeSelection()
                    }
                    is SelectionMode.ResizeSelection -> {}
                }
            }
        }

        return true
    }

    private fun touchDownInSelectionMode(x: Int, y: Int) {
        topLeftX = x
        topLeftY = y
        selectionRect.set(x, y, x, y)

        selectionMode = SelectionMode.Selection
    }

    private fun touchDownInResizeMode(x: Int, y: Int) {
        selectionMode = when {
            topLeftRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(topLeftRect)
            topCenterRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(topCenterRect)
            topRightRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(topRightRect)
            leftEdgeRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(leftEdgeRect)
            rightEdgeRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(rightEdgeRect)
            bottomLeftRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(bottomLeftRect)
            bottomCenterRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(bottomCenterRect)
            bottomRightRect.intersectWithPoint(x, y) ->
                SelectionMode.ResizeSelection(bottomRightRect)
            else ->
                SelectionMode.ResizeSelection()
        }
    }

    private fun touchMoveInSelectionMode(x: Int, y: Int) {
        selectionRect.set(topLeftX, topLeftY, x, y)

        redrawResizeSquares()
    }

    private fun touchMoveInResizeMode(x: Int, y: Int) {
        (selectionMode as SelectionMode.ResizeSelection).rect?.let {
            when (it) {
                topLeftRect ->
                    selectionRect.setOptionally(topX = x, topY = y)
                topCenterRect ->
                    selectionRect.setOptionally(topY = y)
                topRightRect ->
                    selectionRect.setOptionally(topY = y, bottomX = x)
                leftEdgeRect ->
                    selectionRect.setOptionally(topX = x)
                rightEdgeRect ->
                    selectionRect.setOptionally(bottomX = x)
                bottomLeftRect ->
                    selectionRect.setOptionally(topX = x, bottomY = y)
                bottomCenterRect ->
                    selectionRect.setOptionally(bottomY = y)
                bottomRightRect ->
                    selectionRect.setOptionally(bottomX = x, bottomY = y)
                else -> {}
            }
        }
    }

    private fun drawSelectionRectangle() {
        clear()
        mCanvas.drawRect(selectionRect, paintRect)
        mCanvas.drawRect(selectionRect, paintStrokeRect)
        mCanvas.drawRect(topLeftRect, paintChangeSizeRect)
        mCanvas.drawRect(topCenterRect, paintChangeSizeRect)
        mCanvas.drawRect(topRightRect, paintChangeSizeRect)
        mCanvas.drawRect(bottomLeftRect, paintChangeSizeRect)
        mCanvas.drawRect(bottomCenterRect, paintChangeSizeRect)
        mCanvas.drawRect(bottomRightRect, paintChangeSizeRect)
        mCanvas.drawRect(leftEdgeRect, paintChangeSizeRect)
        mCanvas.drawRect(rightEdgeRect, paintChangeSizeRect)
    }

    private fun clear() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    private fun redrawResizeSquares() {
        topLeftRect.set(topLeftX - 20, topLeftY - 20, topLeftX + 20, topLeftY + 20)
        topCenterRect.set(
            selectionRect.width() / 2 + topLeftX - 20,
            topLeftY - 20,
            selectionRect.width() / 2 + topLeftX + 20,
            topLeftY + 20
        )
        topRightRect.set(
            selectionRect.width() + topLeftX - 20,
            topLeftY - 20,
            selectionRect.width() + topLeftX + 20,
            topLeftY + 20
        )
        bottomLeftRect.set(
            topLeftX - 20,
            selectionRect.height() + topLeftY - 20,
            topLeftX + 20,
            selectionRect.height() + topLeftY + 20
        )
        bottomCenterRect.set(
            selectionRect.width() / 2 + topLeftX - 20,
            selectionRect.height() + topLeftY - 20,
            selectionRect.width() / 2 + topLeftX + 20,
            selectionRect.height() + topLeftY + 20
        )
        bottomRightRect.set(
            selectionRect.width() + topLeftX - 20,
            selectionRect.height() + topLeftY - 20,
            selectionRect.width() + topLeftX + 20,
            selectionRect.height() + topLeftY + 20
        )
        leftEdgeRect.set(
            topLeftX - 20,
            selectionRect.height() / 2 + topLeftY - 20,
            topLeftX + 20,
            selectionRect.height() / 2 + topLeftY + 20
        )
        rightEdgeRect.set(
            selectionRect.width() + topLeftX - 20,
            selectionRect.height() / 2 + topLeftY - 20,
            selectionRect.width() + topLeftX + 20,
            selectionRect.height() / 2 + topLeftY + 20
        )
    }

    private fun Rect.setOptionally(
        topX: Int = left,
        topY: Int = top,
        bottomX: Int = right,
        bottomY: Int = bottom
    ) {
        topLeftX = topX
        topLeftY = topY
        set(topX, topY, bottomX, bottomY)
        redrawResizeSquares()
    }
}

sealed class SelectionMode {
    object Waiting : SelectionMode()
    object Selection : SelectionMode()
    data class ResizeSelection(val rect: Rect? = null) : SelectionMode()
}
