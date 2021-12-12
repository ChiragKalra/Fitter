package com.bruhascended.db.friends.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bruhascended.db.weight.entities.MonthlyStats
import com.bruhascended.db.weight.entities.WeeklyStats
import java.io.Serializable

@Entity(tableName = "friends")
data class Friend (
    var email: String,
    var username: String,
    @Embedded
    var weeklyActivity: WeeklyStats,
    @Embedded
    var monthlyActivity: MonthlyStats,
    @PrimaryKey(autoGenerate = true)
    var uid: Long = -1,
): Serializable