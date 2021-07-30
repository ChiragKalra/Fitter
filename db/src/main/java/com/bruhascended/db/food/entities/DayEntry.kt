package com.bruhascended.db.food.entities

import androidx.room.*
import com.bruhascended.db.food.types.NutrientType
import java.io.Serializable
import java.util.*
import kotlin.math.abs

@Entity
data class DayEntry (
    @PrimaryKey
    val day: Long,
    var calories: Int = 0,
    val nutrientInfo: EnumMap<NutrientType, Double> = EnumMap(NutrientType::class.java),
): Serializable {

    operator fun plusAssign(other: FoodEntry) {
        calories += other.entry.calories
        val amountPerQuantity = other.food.weightInfo[other.entry.quantityType]
        if (amountPerQuantity != null) {
            val amount = other.entry.quantity * amountPerQuantity
            other.food.nutrientInfo.forEach { (key, value) ->
                nutrientInfo[key] = (nutrientInfo[key] ?: .0) + value * amount
            }
        }
    }

    operator fun minusAssign(other: FoodEntry) {
        calories -= other.entry.calories
        val amountPerQuantity = other.food.weightInfo[other.entry.quantityType]
        if (amountPerQuantity != null) {
            val amount = other.entry.quantity * amountPerQuantity
            other.food.nutrientInfo.forEach { (key, value) ->
                nutrientInfo[key] = (nutrientInfo[key] ?: .0) - value * amount
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DayEntry
        if (day != other.day) return false
        if (calories != other.calories) return false
        if (day != other.day) return false
        if (nutrientInfo != other.nutrientInfo) return false
        return true
    }

    override fun hashCode(): Int {
        return day.hashCode()
    }

    val id
        get() = abs(hashCode())
}
