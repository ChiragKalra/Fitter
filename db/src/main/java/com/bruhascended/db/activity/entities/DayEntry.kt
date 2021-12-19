package com.bruhascended.db.activity.entities

import androidx.room.*
import java.io.Serializable
import java.util.*
import kotlin.math.abs


@Entity(tableName = "activity_day_entries")
data class DayEntry(
    @PrimaryKey
    var startTime: Long,
    var totalCalories: Float = 0f,
    var totalDuration: Long = 0L,
    var totalDistance: Double = .0,
    var totalSteps: Int = 0,
): Serializable {

    operator fun plusAssign(other: ActivityEntry) {
        totalCalories += other.calories
        totalDuration += other.duration ?: 0L
        totalDistance += other.distance ?: .0
        totalSteps += other.steps ?: 0
    }

    operator fun plus(entry: ActivityEntry) = DayEntry(
        startTime,
        totalCalories + entry.calories,
        totalDuration + (entry.duration ?: 0),
        totalDistance + (entry.distance ?: .0),
        totalSteps + (entry.steps ?: 0),
    )

    operator fun minusAssign(other: ActivityEntry) {
        totalCalories -= other.calories
        totalDuration -= other.duration ?: 0L
        totalDistance -= other.distance ?: .0
        totalSteps -= other.steps ?: 0
    }

    operator fun minus(entry: ActivityEntry) = DayEntry(
        startTime,
        totalCalories - entry.calories,
        totalDuration - (entry.duration ?: 0),
        totalDistance - (entry.distance ?: .0),
        totalSteps - (entry.steps ?: 0),
    )

    operator fun plusAssign(other: DayEntry) {
        totalCalories += other.totalCalories
        totalDuration += other.totalDuration
        totalDistance += other.totalDistance
        totalSteps += other.totalSteps
    }

    operator fun plus(entry: DayEntry) = DayEntry(
        startTime,
        totalCalories + entry.totalCalories,
        totalDuration + entry.totalDuration,
        totalDistance + entry.totalDistance,
        totalSteps + entry.totalSteps,
    )

    operator fun minusAssign(other: DayEntry) {
        totalCalories -= other.totalCalories
        totalDuration -= other.totalDuration
        totalDistance -= other.totalDistance
        totalSteps -= other.totalSteps
    }

    operator fun minus(entry: DayEntry) = DayEntry(
        startTime,
        totalCalories - entry.totalCalories,
        totalDuration - entry.totalDuration,
        totalDistance - entry.totalDistance,
        totalSteps - entry.totalSteps,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DayEntry
        if (startTime != other.startTime) return false
        if (totalCalories != other.totalCalories) return false
        if (totalDuration != other.totalDuration) return false
        if (totalSteps != other.totalSteps) return false
        if (totalDistance != other.totalDistance) return false
        return true
    }

    override fun hashCode(): Int {
        return startTime.hashCode()
    }

    val date: Date
        get() = Calendar.getInstance().also {
            it.timeInMillis = startTime
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }.time

    val id
        get() = abs(hashCode())
}