package com.example.api.models.foods

data class FoodSearchCriteria(
    val dataType: List<String>,
    val foodTypes: List<String>,
    val generalSearchInput: String,
    val numberOfResultsPerPage: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val query: String,
    val requireAllWords: Boolean
)