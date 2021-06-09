package com.bruhascended.db.food

import androidx.room.TypeConverter
import com.google.gson.Gson

internal class FoodEntryConverters {
    @TypeConverter
    fun listToJson (value: Array<Double>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToList (value: String?): Array<Double> = Gson().fromJson(
        value,
        Array<Double>::class.java
    )

    @TypeConverter
    fun toQuantityType (value: Int) = enumValues<QuantityType>()[value]

    @TypeConverter
    fun fromQuantityType (value: QuantityType) = value.ordinal
}