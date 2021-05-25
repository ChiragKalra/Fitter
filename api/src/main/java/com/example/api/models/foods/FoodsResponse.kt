package com.example.api.models.foods

import com.example.api.models.foods.Aggregations
import com.example.api.models.foods.Food

data class FoodsResponse(
    val aggregations: Aggregations,
    val currentPage: Int,
    val foods: List<Food>,
    val totalHits: Int,
    val totalPages: Int
)