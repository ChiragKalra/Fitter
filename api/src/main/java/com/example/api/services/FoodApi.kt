package com.example.api.services

import com.example.api.models.food.FoodResponse
import com.example.api.models.foods.FoodsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

private const val KEY: String = "Vy1XpFHUzBIXF0SF2UbVQVBLmv8ZhwgSTxP60Jec"

interface FoodApi {
    @GET("foods/search?api_key=${KEY}")
    fun getFoods(
        @Query("query") query: String,
        @Query("dataType") dataType: List<String> = listOf<String>("Survey (FNDDS)", "Branded"),
        @Query("requireAllWords") requireAllWords: String = "false",
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageNumber") pageNumber: Int = 1
    ): Call<FoodsResponse>

    @GET("food/1731228?api_key=${KEY}")
    fun getFood(): Call<FoodResponse>
}