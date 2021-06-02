package com.bruhascended.classifier.foodimage

import android.content.Context
import android.graphics.Bitmap
import com.bruhascended.classifier.ml.AiyVisionClassifierFoodV1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model

class ImageClassifier (context: Context) {

    val outputCount = 2024

    private var model: AiyVisionClassifierFoodV1
    private var imageProcessor: ImageProcessor

    init {
        val compatList = CompatibilityList()
        val device = if (compatList.isDelegateSupportedOnThisDevice)
            Model.Device.GPU else Model.Device.CPU

        val options = Model.Options.Builder()
            .setNumThreads(6)
            .setDevice(device)
            .build()

        model = AiyVisionClassifierFoodV1.newInstance(context, options)

        imageProcessor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    224,
                    224,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(Rot90Op(1))
            .build()
    }

    fun fetchResults (bitmap: Bitmap): Array<Category> {
        var tensorImage = TensorImage(DataType.UINT8)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Runs model inference and gets result.
        val outputs = model.process(tensorImage)
        return outputs.probabilityAsCategoryList.toTypedArray()
    }

    fun close() {
        // Releases model resources if no longer used.
        model.close()
    }
}