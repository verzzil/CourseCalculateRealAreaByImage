package com.xannanov.course.utils

import android.graphics.Rect

fun Rect.intersectWithPoint(x: Int, y: Int): Boolean =
    x in left..right && y in top..bottom
