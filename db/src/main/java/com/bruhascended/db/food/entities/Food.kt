package com.bruhascended.db.food.entities

import androidx.room.*
import com.bruhascended.db.food.QuantityType
import java.io.Serializable
import kotlin.math.abs

@Entity
data class Food (
    @PrimaryKey
    val foodName: String,
    val description: String? = null,
    val healthRating: Int = 0,
    val calorieInfoArray: Array<Float> = Array(QuantityType.values().size) { -1f }
): Serializable {

    fun setCalorieInfo (quantityType: QuantityType, value: Float) {
        calorieInfoArray[quantityType.ordinal] = value
    }

    fun setCalorieInfo (pairs: HashMap<QuantityType, Float>) {
        pairs.forEach { (q, v) ->
            calorieInfoArray[q.ordinal] = v
        }
    }

    fun getCalorieInfo(): HashMap<QuantityType, Float> {
        return HashMap<QuantityType, Float>().apply {
            calorieInfoArray.forEachIndexed { i, v ->
                if (v != -1f) {
                    put(QuantityType.values()[i], v)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Food
        if (foodName != other.foodName) return false
        if (description != other.description) return false
        if (healthRating != other.healthRating) return false
        if (!calorieInfoArray.contentEquals(other.calorieInfoArray)) return false
        return true
    }

    override fun hashCode(): Int {
        return foodName.hashCode()
    }

    val id
        get() = abs(hashCode())
}
