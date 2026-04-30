package com.bruhascended.classifier.foodimage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect

fun Bitmap.addSquarePadding(dimension: Int): Bitmap {
    val outputBitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.RGB_565)
    val canvas = Canvas(outputBitmap)
    canvas.drawColor(Color.BLACK)
    val srcRect = Rect(0, 0, width, height)
    val destRect = if (width > height) {
        val newHeight = dimension.toFloat() * height.toFloat()/width.toFloat()
        val padding = ((dimension - newHeight)/2).toInt()
        Rect(0, padding, dimension, dimension-padding)
    } else {
        val newWidth = dimension.toFloat() * width.toFloat()/height.toFloat()
        val padding = ((dimension - newWidth)/2).toInt()
        Rect(padding,0, dimension-padding, dimension)
    }
    canvas.drawBitmap(this, srcRect, destRect, null)
    return outputBitmap
}
