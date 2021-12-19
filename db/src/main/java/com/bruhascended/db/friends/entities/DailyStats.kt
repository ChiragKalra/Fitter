package com.bruhascended.db.friends.entities

import androidx.room.ColumnInfo
import java.io.Serializable
import kotlin.math.abs

data class DailyStats(
    @ColumnInfo(name = "daily_total_calories")
    var totalCalories: Int = 0,
    @ColumnInfo(name = "daily_total_duration")
    var totalDuration: Int = 0,
    @ColumnInfo(name = "daily_total_distance")
    var totalDistance: Float = 0f,
    @ColumnInfo(name = "daily_total_steps")
    var totalSteps: Int = 0,
): Serializable {

    operator fun plusAssign(other: DailyStats) {
        totalCalories += other.totalCalories
        totalDuration += other.totalDuration
        totalDistance += other.totalDistance
        totalSteps += other.totalSteps
    }

    operator fun plus(entry: DailyStats) = DailyStats(
        totalCalories + entry.totalCalories,
        totalDuration + entry.totalDuration,
        totalDistance + entry.totalDistance,
        totalSteps + entry.totalSteps,
    )

    operator fun minusAssign(other: DailyStats) {
        totalCalories -= other.totalCalories
        totalDuration -= other.totalDuration
        totalDistance -= other.totalDistance
        totalSteps -= other.totalSteps
    }

    operator fun minus(entry: DailyStats) = DailyStats(
        totalCalories - entry.totalCalories,
        totalDuration - entry.totalDuration,
        totalDistance - entry.totalDistance,
        totalSteps - entry.totalSteps,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DailyStats
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