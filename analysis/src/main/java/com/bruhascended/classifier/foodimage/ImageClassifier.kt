package com.bruhascended.classifier.foodimage

import android.content.Context
import android.graphics.Bitmap
import com.bruhascended.classifier.ml.MobilenetV3LargeFoodClassifier
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model

class ImageClassifier (
    context: Context,
    private val useIndian: Boolean = true
) {

    private val outputCountAmerican = 2024
    private val americanInputDim = 196

    private val outputCountIndian = 296
    private val indianInputDim = 224

    val outputCount: Int
    get() = if (useIndian) outputCountIndian else outputCountAmerican

    private var indianModel: MobilenetV3LargeFoodClassifier

    private var imageProcessorAmerican: ImageProcessor
    private var imageProcessorIndian: ImageProcessor

    init {
        val optionsCpu = Model.Options.Builder()
            .setNumThreads(6)
            .setDevice(Model.Device.CPU)
            .build()

        indianModel = MobilenetV3LargeFoodClassifier.newInstance(context, optionsCpu)

        imageProcessorAmerican = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    americanInputDim,
                    americanInputDim,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(Rot90Op(-1))
            .build()
        imageProcessorIndian = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    indianInputDim,
                    indianInputDim,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(Rot90Op(-1))
            .build()
    }

    fun fetchResults(bitmap: Bitmap): Array<Category> {
        val tensorImage = imageProcessorIndian.process(
            TensorImage(DataType.FLOAT32).apply { load(bitmap) }
        )
        return indianModel.process(tensorImage).probabilityAsCategoryList.toTypedArray()
    }

    fun close() {
        // Releases model resources if no longer used.
        indianModel.close()
    }
}