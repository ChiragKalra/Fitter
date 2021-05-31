package com.bruhascended.api.services

import com.bruhascended.api.models.food.FoodResponse
import com.bruhascended.api.models.foods.FoodsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

private const val KEY: String = ""

interface FoodApi {

    @GET("foods/search?api_key=${KEY}")
    suspend fun getFoods(
        @Query("query") query: String,
        @Query("dataType") dataType: List<String> = listOf<String>("Survey (FNDDS)", "Branded"),
        @Query("requireAllWords") requireAllWords: String = "false",
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNumber") pageNumber: Int = 1
    ): Response<FoodsResponse>

    @GET("food/1731228?api_key=${KEY}")
    suspend fun getFood(): Response<FoodResponse>
}