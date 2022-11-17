package com.xannanov.course.workview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class IndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var chunkSize = 0f

    private var numbers = IndicatorViewNumber.createFromValue(81478)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = resolveHeight(widthMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        chunkSize = w / 5f - STROKE_WIDTH * 5
    }

    override fun onDraw(canvas: Canvas) {
        drawNumbers(canvas)
    }

    fun setValue(value: Int) {
        numbers = IndicatorViewNumber.createFromValue(value)
        invalidate()
    }

    private fun resolveHeight(widthMeasureSpec: Int): Int {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        return ((width / 5f - STROKE_WIDTH * 5) * 2 + STROKE_WIDTH * 5).toInt()
    }

    private fun drawNumbers(canvas: Canvas) {
        var offsetX = STROKE_WIDTH / 2 + getStartOffset()
        var flag = false
        numbers.forEachIndexed { index, indicatorViewNumber ->
            if (indicatorViewNumber.number != 0 || index == numbers.size - 1)
                flag = true
            indicatorViewNumber.drawNumber(offsetX, chunkSize, canvas, flag)
            offsetX += chunkSize + STROKE_WIDTH * 3 + STROKE_WIDTH * 2
        }
    }

    private fun getStartOffset() =
        (width - (chunkSize * 5 + STROKE_WIDTH * 23)) / 2

    private class IndicatorViewNumber(val number: Int) {

        private val paintDefault = Paint().apply {
            color = 0xffF3F3F3.toInt()
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH
            strokeCap = Paint.Cap.ROUND
        }

        private val paintRed = Paint().apply {
            color = 0xffD82325.toInt()
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH
            strokeCap = Paint.Cap.ROUND
        }

        private val paths: List<Path> = arrayListOf(
            Path(), // top
            Path(), // top right
            Path(), // bottom right
            Path(), // bottom
            Path(), // bottom left
            Path(), // top left
            Path(), // middle
        )

        fun drawNumber(startXOffset: Float, chunkSize: Float, canvas: Canvas, flag: Boolean) {
            drawTopSide(startXOffset + STROKE_WIDTH, chunkSize, canvas, flag)
            drawRightSide(startXOffset + STROKE_WIDTH * 2, chunkSize, canvas, flag)
            drawBottomSide(startXOffset + STROKE_WIDTH, chunkSize, canvas, flag)
            drawLeftSide(startXOffset, chunkSize, canvas, flag)
            drawMiddleSide(startXOffset + STROKE_WIDTH, chunkSize, canvas, flag)
        }

        private fun drawTopSide(offset: Float, chunkSize: Float, canvas: Canvas, flag: Boolean) {
            paths[0].moveTo(offset, STROKE_WIDTH / 2)
            paths[0].lineTo(offset + chunkSize, STROKE_WIDTH / 2)
            if (flag)
                when (number) {
                    2, 3, 5, 6, 7, 8, 9, 0 ->
                        canvas.drawPath(paths[0], paintRed)
                    else ->
                        canvas.drawPath(paths[0], paintDefault)
                }
            else
                canvas.drawPath(paths[0], paintDefault)
        }

        private fun drawRightSide(
            startXOffset: Float,
            chunkSize: Float,
            canvas: Canvas,
            flag: Boolean
        ) {
            paths[1].moveTo(chunkSize + startXOffset, STROKE_WIDTH * 1.5f)
            paths[1].lineTo(chunkSize + startXOffset, STROKE_WIDTH * 1.5f + chunkSize)
            if (flag)
                when (number) {
                    1, 2, 3, 4, 7, 8, 9, 0 ->
                        canvas.drawPath(paths[1], paintRed)
                    else ->
                        canvas.drawPath(paths[1], paintDefault)
                }
            else
                canvas.drawPath(paths[1], paintDefault)

            paths[2].moveTo(
                chunkSize + startXOffset,
                STROKE_WIDTH * 1.5f + chunkSize + STROKE_WIDTH * 2
            )
            paths[2].lineTo(
                chunkSize + startXOffset,
                STROKE_WIDTH * 1.5f + chunkSize + STROKE_WIDTH * 2 + chunkSize
            )
            if (flag)
                when (number) {
                    1, 3, 4, 5, 6, 7, 8, 9, 0 ->
                        canvas.drawPath(paths[2], paintRed)
                    else ->
                        canvas.drawPath(paths[2], paintDefault)
                }
            else
                canvas.drawPath(paths[2], paintDefault)
        }

        private fun drawBottomSide(
            startXOffset: Float,
            chunkSize: Float,
            canvas: Canvas,
            flag: Boolean
        ) {
            paths[3].moveTo(startXOffset, chunkSize * 2 + STROKE_WIDTH * 4.5f)
            paths[3].lineTo(startXOffset + chunkSize, chunkSize * 2 + STROKE_WIDTH * 4.5f)

            if (flag)
                when (number) {
                    2, 3, 5, 6, 8, 9, 0 ->
                        canvas.drawPath(paths[3], paintRed)
                    else ->
                        canvas.drawPath(paths[3], paintDefault)
                }
            else
                canvas.drawPath(paths[3], paintDefault)
        }

        private fun drawLeftSide(offset: Float, chunkSize: Float, canvas: Canvas, flag: Boolean) {
            paths[4].moveTo(offset, STROKE_WIDTH * 1.5f + chunkSize + STROKE_WIDTH * 2)
            paths[4].lineTo(
                offset,
                STROKE_WIDTH * 1.5f + chunkSize + STROKE_WIDTH * 2 + chunkSize
            )
            if (flag)
                when (number) {
                    2, 6, 8, 0 ->
                        canvas.drawPath(paths[4], paintRed)
                    else ->
                        canvas.drawPath(paths[4], paintDefault)
                }
            else
                canvas.drawPath(paths[4], paintDefault)

            paths[5].moveTo(offset, STROKE_WIDTH * 1.5f)
            paths[5].lineTo(offset, STROKE_WIDTH * 1.5f + chunkSize)
            if (flag)
                when (number) {
                    4, 5, 6, 8, 9, 0 ->
                        canvas.drawPath(paths[5], paintRed)
                    else ->
                        canvas.drawPath(paths[5], paintDefault)
                }
            else
                canvas.drawPath(paths[5], paintDefault)
        }

        private fun drawMiddleSide(
            startXOffset: Float,
            chunkSize: Float,
            canvas: Canvas,
            flag: Boolean
        ) {
            paths[6].moveTo(startXOffset, chunkSize + STROKE_WIDTH * 2.5f)
            paths[6].lineTo(startXOffset + chunkSize, chunkSize + STROKE_WIDTH * 2.5f)
            if (flag)
                when (number) {
                    2, 3, 4, 5, 6, 8, 9 ->
                        canvas.drawPath(paths[6], paintRed)
                    else ->
                        canvas.drawPath(paths[6], paintDefault)
                }
            else
                canvas.drawPath(paths[6], paintDefault)
        }

        companion object {

            fun createFromValue(value: Int): List<IndicatorViewNumber> =
                arrayListOf<IndicatorViewNumber>().apply {
                    var i = 10000
                    while (i >= 1) {
                        add(IndicatorViewNumber(value / i % 10))
                        i /= 10
                    }
                }
        }
    }

    companion object {
        private const val STROKE_WIDTH = 25f
    }
}