package com.bruhascended.api.services

import com.bruhascended.api.BuildConfig
import com.bruhascended.api.models.foodsv2.Foodsv2Response
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY: String = BuildConfig.EDAMAM_API_KEY // provide your own path
private const val APP_ID: String = BuildConfig.EDAMAM_API_ID // provide your own path

interface FoodApi {

    @GET("parser")
    suspend fun getFoodsv2(
        @Query("ingr") query: String,
        @Query("app_id") app_id: String = APP_ID,
        @Query("app_key") app_key: String = API_KEY
    ): Response<Foodsv2Response?>
}