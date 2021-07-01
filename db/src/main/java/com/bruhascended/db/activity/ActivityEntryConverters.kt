package com.bruhascended.db.activity

import androidx.room.TypeConverter
import com.bruhascended.db.activity.types.ActivityType

internal class ActivityEntryConverters {
    @TypeConverter
    fun toActivityType (value: Int) = enumValues<ActivityType>()[value]

    @TypeConverter
    fun fromActivityType (value: ActivityType) = value.ordinal
}