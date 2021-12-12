package com.bruhascended.db.weight.entities

import java.io.Serializable
import kotlin.math.abs

data class WeeklyStats(
    var totalCalories: Float = 0f,
    var totalDuration: Long = 0L,
    var totalDistance: Double = .0,
    var totalSteps: Int = 0,
): Serializable {

    operator fun plusAssign(other: WeeklyStats) {
        totalCalories += other.totalCalories
        totalDuration += other.totalDuration
        totalDistance += other.totalDistance
        totalSteps += other.totalSteps
    }

    operator fun plus(entry: WeeklyStats) = WeeklyStats(
        totalCalories + entry.totalCalories,
        totalDuration + entry.totalDuration,
        totalDistance + entry.totalDistance,
        totalSteps + entry.totalSteps,
    )

    operator fun minusAssign(other: WeeklyStats) {
        totalCalories -= other.totalCalories
        totalDuration -= other.totalDuration
        totalDistance -= other.totalDistance
        totalSteps -= other.totalSteps
    }

    operator fun minus(entry: WeeklyStats) = WeeklyStats(
        totalCalories - entry.totalCalories,
        totalDuration - entry.totalDuration,
        totalDistance - entry.totalDistance,
        totalSteps - entry.totalSteps,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WeeklyStats
        if (totalCalories != other.totalCalories) return false
        if (totalDuration != other.totalDuration) return false
        if (totalSteps != other.totalSteps) return false
        if (totalDistance != other.totalDistance) return false
        return true
    }

    override fun hashCode(): Int {
        var result = totalCalories.hashCode()
        result = 31 * result + totalDuration.hashCode()
        result = 31 * result + totalDistance.hashCode()
        result = 31 * result + totalSteps
        return result
    }

    val id
        get() = abs(hashCode())
}