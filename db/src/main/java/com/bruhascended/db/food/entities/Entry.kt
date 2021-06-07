package com.bruhascended.db.food.entities

import androidx.room.*
import com.bruhascended.db.food.QuantityType
import java.io.Serializable

@Entity
data class Entry (
    val calories: Float,
    val quantity: Float,
    val quantityType: QuantityType,
    val time: Long,
    @PrimaryKey(autoGenerate = true)
    var entryId: Long = -1,
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entry
        if (quantity != other.quantity) return false
        if (quantityType != other.quantityType) return false
        if (time != other.time) return false
        return true
    }

    override fun hashCode(): Int {
        return entryId.hashCode()
    }
}
