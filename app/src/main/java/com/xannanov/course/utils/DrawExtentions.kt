package com.xannanov.course.utils

import android.graphics.Rect
import android.graphics.RectF

fun Rect.intersectWithPoint(x: Int, y: Int): Boolean =
    x in left..right && y in top..bottom

fun RectF.intersectWithPoint(x: Float, y: Float): Boolean =
    x in left..right && y in top..bottom
