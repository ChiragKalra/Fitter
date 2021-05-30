package com.example.api.models.foods

data class FoodsResponse(
    val foods: List<Food>,
    val totalHits: Int,
)