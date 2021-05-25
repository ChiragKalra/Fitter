package com.bruhascended.classifier

import android.content.Context
import android.graphics.Bitmap
import com.bruhascended.classifier.ml.AiyVisionClassifierFoodV1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.model.Model
import kotlin.math.min


class FoodImageClassifier (
    private val context: Context
) {

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
            .build()
    }


    fun fetchResults (bitmap: Bitmap): Array<String> {

        var tensorImage = TensorImage(DataType.UINT8)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Runs model inference and gets result.
        val outputs = model.process(tensorImage)
        val probability = outputs.probabilityAsCategoryList
        probability.sortByDescending { it.score }

        return Array(min(5, probability.size)) {
            probability[it].label
        }
    }

    fun close() {
        // Releases model resources if no longer used.
        model.close()
    }
}