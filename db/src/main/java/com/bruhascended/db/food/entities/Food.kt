package com.bruhascended.db.food.entities

import androidx.room.*
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import java.io.Serializable
import java.util.*
import kotlin.math.abs

@Entity
data class Food (
    @PrimaryKey
    val foodName: String,
    val calories: Double,
    val weightInfo: EnumMap<QuantityType, Double> = EnumMap(QuantityType::class.java),
    val nutrientInfo: EnumMap<NutrientType, Double> = EnumMap(NutrientType::class.java),
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Food
        if (foodName != other.foodName) return false
        if (calories != other.calories) return false
        if (weightInfo != other.weightInfo) return false
        if (nutrientInfo != other.nutrientInfo) return false
        return true
    }

    override fun hashCode(): Int {
        return foodName.hashCode()
    }

    val id
        get() = abs(hashCode())
}
