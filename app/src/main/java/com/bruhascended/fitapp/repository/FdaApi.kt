package com.bruhascended.fitapp.repository

import com.bruhascended.api.FoodClient

object FdaApi {
    // fun to fetch foods when search query provided
    suspend fun fetchFoods(query: String) = FoodClient.fdaApi.getFoods(query)
}