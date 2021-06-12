package com.bruhascended.fitapp.repository

import com.bruhascended.api.FoodClient

object FdaApi {
    // fun to fetch foods from v2
    suspend fun fetchFoodsv2(query: String) = FoodClient.fdaApi.getFoodsv2(query)
}