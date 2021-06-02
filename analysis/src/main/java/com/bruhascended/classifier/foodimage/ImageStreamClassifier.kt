package com.bruhascended.classifier.foodimage

import android.content.Context
import android.graphics.Bitmap
import com.bruhascended.classifier.RunTimeAnalyzer
import org.tensorflow.lite.support.label.Category
import kotlin.math.max
import kotlin.math.pow

class ImageStreamClassifier(
    context: Context,
    private val expectedLatency: Long,
    private val predictionFadeDuration: Long
) {

    companion object {
        const val EXP_AVG_EXPONENT = 4
        const val EXP_AVG_BETA_UPPER_BOUND = 0.95
        const val EXP_AVG_BETA_LOWER_BOUND = 0.70
        const val SCORE_LOWER_BOUND = 0.55
    }

    private var classifier: ImageClassifier = ImageClassifier(context)
    private val runTimeAnalyzer = RunTimeAnalyzer()

    private var weightedAverages: Array<Double> = Array(classifier.outputCount) {0.0}
    private var runningBeta = 1.0

    fun fetchResults (bitmap: Bitmap): Array<String> {
        runTimeAnalyzer.log()
        val categories = classifier.fetchResults(bitmap)
        runningAverage(categories)
        return getProcessedPredictions(categories)
    }

    fun getLatencyCorrection() = max(
        0,
        expectedLatency - (runTimeAnalyzer.movingAverage?.toLong() ?: 0)
    )

    private fun runningAverage (categories: Array<Category>) {
        val currBeta = getBeta()
        runningBeta *= currBeta
        for (i in weightedAverages.indices) {
            val curr = categories[i].score.pow(EXP_AVG_EXPONENT)
            weightedAverages[i] = weightedAverages[i]*currBeta + curr*(1-currBeta)
        }
    }

    private fun getProcessedPredictions (categories: Array<Category>): Array<String> {
        val indexes = HashMap<String, Int>().apply {
            categories.forEachIndexed { i, it ->
                put(it.label, i)
            }
        }
        categories.sortByDescending {
            weightedAverages[indexes[it.label]!!] / (1 - runningBeta)
        }

        val filteredCategories = categories.filter {
            it.score > SCORE_LOWER_BOUND.pow(EXP_AVG_EXPONENT)
        }.toTypedArray()

        return Array(filteredCategories.size) {
            filteredCategories[it].label
        }
    }

    /*
        * return the number of prediction records to store in memory to compute a combined probability
        * @param
        *  the time length that has to be considered
     */
    private fun getBeta(): Double {
        val time = runTimeAnalyzer.movingAverage ?: expectedLatency.toDouble()
        val beta = 1 - time / predictionFadeDuration
        if (beta > EXP_AVG_BETA_UPPER_BOUND) return EXP_AVG_BETA_UPPER_BOUND
        if (beta < EXP_AVG_BETA_LOWER_BOUND) return EXP_AVG_BETA_LOWER_BOUND
        return beta
    }

    fun close() {
        classifier.close()
    }

}