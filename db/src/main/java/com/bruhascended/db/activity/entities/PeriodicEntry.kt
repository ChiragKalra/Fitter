package com.bruhascended.db.activity.entities

import androidx.room.*
import java.io.Serializable
import kotlin.math.abs


@Entity
data class PeriodicEntry (
    @PrimaryKey
    val startTime: Long,
    var totalCalories: Int = 0,
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

    operator fun plus(entry: ActivityEntry) = PeriodicEntry(
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

    operator fun minus(entry: ActivityEntry) = PeriodicEntry(
        startTime,
        totalCalories - entry.calories,
        totalDuration - (entry.duration ?: 0),
        totalDistance - (entry.distance ?: .0),
        totalSteps - (entry.steps ?: 0),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PeriodicEntry
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

    val id
        get() = abs(hashCode())
}