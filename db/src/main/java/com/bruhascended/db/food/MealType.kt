package com.bruhascended.db.food

enum class MealType(
    val hoursOfDay: IntRange? = null
) {
    Breakfast(5 until 11),
    Brunch(11 until 1),
    Lunch(1 until 3),
    EveningSnacks(4 until 7),
    Dinner(7 until 11),
    LateNightSnacks(11 until 4),
    Other()
}