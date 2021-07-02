package com.bruhascended.db.weight.entities

import androidx.room.*
import com.bruhascended.db.weight.types.WeightType
import java.io.Serializable
import java.util.*

@Entity(tableName = "weight_entry")
data class WeightEntry (
    val weight: Double,
    val type: WeightType,
    val timeInMillis: Long,
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1,
): Serializable {

    val date: Date
    get() = Calendar.getInstance().also {
            it.timeInMillis = timeInMillis
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }.time

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WeightEntry
        if (weight != other.weight) return false
        if (type != other.type) return false
        if (timeInMillis != other.timeInMillis) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
