package com.bruhascended.db.food

import androidx.room.TypeConverter
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import java.util.*

internal class FoodEntryConverters {

    @TypeConverter
    fun fromQuantityTypeMap (value: EnumMap<QuantityType, Double>) = value.toString()

    @TypeConverter
    fun toQuantityTypeMap (str: String): EnumMap<QuantityType, Double> {
        val genMap = EnumMap<QuantityType, Double>(QuantityType::class.java)
        str.slice(1 until str.lastIndex).split(',').forEach {
            if (it.isBlank()) return@forEach
            val got = it.trim().split('=')
            genMap[QuantityType.valueOf(got[0])] = got[1].toDouble()
        }
        return genMap
    }


    @TypeConverter
    fun fromNutrientTypeMap (value: EnumMap<NutrientType, Double>) = value.toString()

    @TypeConverter
    fun toNutrientTypeMap (str: String): EnumMap<NutrientType, Double> {
        val genMap = EnumMap<NutrientType, Double>(NutrientType::class.java)
        str.slice(1 until str.lastIndex).split(',').forEach {
            if (it.isBlank()) return@forEach
            val got = it.trim().split('=')
            genMap[NutrientType.valueOf(got[0])] = got[1].toDouble()
        }
        return genMap
    }


    @TypeConverter
    fun toQuantityType (value: Int) = enumValues<QuantityType>()[value]

    @TypeConverter
    fun fromQuantityType (value: QuantityType) = value.ordinal


    @TypeConverter
    fun toMealType (value: Int) = enumValues<MealType>()[value]

    @TypeConverter
    fun fromMealType (value: MealType) = value.ordinal
}