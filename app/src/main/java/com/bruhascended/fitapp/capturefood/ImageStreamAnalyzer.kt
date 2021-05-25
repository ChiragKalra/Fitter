package com.bruhascended.fitapp.capturefood

import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bruhascended.classifier.ImageStreamClassifier
import java.io.ByteArrayOutputStream
import kotlin.math.min

class ImageStreamAnalyzer (
    private val context: Context,
    private val listener: (predictions: Array<String>) -> Unit
): ImageAnalysis.Analyzer {

    private var classifier: ImageStreamClassifier = ImageStreamClassifier(context)

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