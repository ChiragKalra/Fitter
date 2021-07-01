package com.bruhascended.db.activity.entities

import androidx.room.*
import com.bruhascended.db.activity.types.ActivityType
import java.io.Serializable
import java.util.*

@Entity(tableName = "activity_entry")
data class ActivityEntry (
    val activity: ActivityType,
    val calories: Int,
    val startTime: Long,
    val duration: Long? = null,
    val description: String? = null,
    val distance: Int? = null,
    val steps: Int? = null,
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1,
): Serializable {

    val date: Date
    get() = Calendar.getInstance().also {
            it.timeInMillis = startTime
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }.time

    val endTime: Long
    get() = startTime + endTime


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityEntry
        if (activity != other.activity) return false
        if (calories != other.calories) return false
        if (startTime != other.startTime) return false
        if (steps != other.steps) return false
        if (duration != other.duration) return false
        if (distance != other.distance) return false
        if (description != other.description) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
