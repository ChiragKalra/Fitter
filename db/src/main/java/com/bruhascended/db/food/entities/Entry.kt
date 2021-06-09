package com.bruhascended.db.food.entities

import androidx.room.*
import com.bruhascended.db.food.QuantityType
import java.io.Serializable

@Entity
data class Entry (
    val calories: Double,
    val quantity: Double,
    val quantityType: QuantityType,
    val timeInMillis: Long,
    @PrimaryKey(autoGenerate = true)
    var entryId: Long? = null,
): Serializable {

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
