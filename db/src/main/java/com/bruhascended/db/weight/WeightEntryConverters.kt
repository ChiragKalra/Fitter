package com.bruhascended.db.weight

import androidx.room.TypeConverter
import com.bruhascended.db.weight.types.WeightType

internal class WeightEntryConverters {
    @TypeConverter
    fun toWeightType (value: Int) = enumValues<WeightType>()[value]

    @TypeConverter
    fun fromWeightType (value: WeightType) = value.ordinal
}