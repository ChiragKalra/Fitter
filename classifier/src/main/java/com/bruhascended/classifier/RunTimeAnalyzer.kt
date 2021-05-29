package com.bruhascended.classifier

import android.util.Log
import java.util.*

class RunTimeAnalyzer {
    private var v = 0.0
    private var startTime = -1L
    private val beta = 0.95
    private var betaExp = 1.0

    val movingAverage: Double?
        get() = if (v == 0.0) null else v / (1 - betaExp)

    fun log() {
        val time = Calendar.getInstance().timeInMillis
        if (startTime == -1L) {
            startTime = time
        } else {
            val u = time - startTime
            v = v * beta + u * (1 - beta)
            betaExp *= beta
            Log.d("Average Latency", movingAverage.toString())
            startTime = time
        }
    }
}