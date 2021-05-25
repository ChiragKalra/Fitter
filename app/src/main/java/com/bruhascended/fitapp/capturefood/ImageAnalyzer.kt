package com.bruhascended.fitapp.capturefood

import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bruhascended.classifier.FoodImageClassifier
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.min

typealias PredictionListener = (predictions: Array<String>) -> Unit

class ImageAnalyzer(
    private val context: Context,
    private val listener: PredictionListener
): ImageAnalysis.Analyzer {

    private var classifier: FoodImageClassifier = FoodImageClassifier(context)

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze (proxy: ImageProxy) {
        var bm = proxy.image?.toBitmap()
        if (bm != null) {
            val dim = min(bm.width, bm.height)
            bm = Bitmap.createBitmap(bm, 0, 0, dim, dim)
            val predictions = classifier.fetchResults(bm)
            listener(predictions)
        }
        proxy.close()
    }

    fun close () {
        classifier.close()
    }
}