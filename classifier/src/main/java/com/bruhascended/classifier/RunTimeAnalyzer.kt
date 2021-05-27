package com.bruhascended.classifier

import java.util.*

class RunTimeAnalyzer {
    private var total = 0L
    private var count = 0

    private var startTime = -1L

    fun log() {
        val time = Calendar.getInstance().timeInMillis
        if (startTime == -1L) {
            startTime = time
        } else {
            total += time - startTime
            startTime = time
            count++
        }
    }

    fun getAverage() : Long? {
        return if (count == 0) null
        else total/count
    }
}