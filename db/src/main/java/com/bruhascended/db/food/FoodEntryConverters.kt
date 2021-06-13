package com.bruhascended.db.food

import androidx.room.TypeConverter

internal class FoodEntryConverters {
    @TypeConverter
    fun listToJson (arr: Array<Double>?): String = StringBuilder().apply {
        arr?.forEach {
            append(it)
            append(',')
        }
    }.toString()

    @TypeConverter
    fun jsonToList (value: String?): Array<Double> {
        return if (value.isNullOrBlank()) {
            return emptyArray()
        } else {
            arrayListOf<Double>().apply {
                value.split(',').forEach {
                    if (it.isNotBlank()) {
                        add(it.toDouble())
                    }
                }
            }.toTypedArray()
        }
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