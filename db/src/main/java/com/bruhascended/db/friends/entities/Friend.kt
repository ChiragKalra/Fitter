package com.bruhascended.db.friends.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "friends")
data class Friend (
    var email: String,
    var username: String,
    @Embedded
    var dailyActivity: DailyStats,
    @Embedded
    var weeklyActivity: WeeklyStats,
    @Embedded
    var monthlyActivity: MonthlyStats,
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1,
): Serializable