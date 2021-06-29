package com.bruhascended.db.food.entities

import androidx.room.*
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import java.io.Serializable
import java.util.*

@Entity
data class Entry (
    val calories: Int,
    val quantity: Double,
    val quantityType: QuantityType,
    val mealType: MealType,
    val timeInMillis: Long,
    @PrimaryKey(autoGenerate = true)
    var entryId: Long? = null,
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

        other as Entry
        if (quantity != other.quantity) return false
        if (quantityType != other.quantityType) return false
        if (timeInMillis != other.timeInMillis) return false
        return true
    }

    override fun hashCode(): Int {
        return entryId.hashCode()
    }
}
