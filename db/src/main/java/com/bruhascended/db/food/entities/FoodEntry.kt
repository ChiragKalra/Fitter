package com.bruhascended.db.food.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import java.io.Serializable

data class FoodEntry(
    @Embedded
    val entry: Entry,
    @Relation(
        parentColumn = "entryId",
        entityColumn = "foodName",
        associateBy = Junction(CrossReference::class)
    )
    val food: Food
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FoodEntry
        if (entry != other.entry) return false
        if (food != other.food) return false
        return true
    }

    override fun hashCode(): Int {
        return entry.hashCode()
    }
}