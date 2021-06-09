package com.bruhascended.db.food.entities

import androidx.room.Entity

@Entity(primaryKeys = ["entryId", "entryId"])
data class CrossReference (
    val entryId: Long,
    val foodName: String
) {
    constructor (foodEntry: FoodEntry) : this(
        foodEntry.entry.entryId!!, foodEntry.food.foodName
    )
}