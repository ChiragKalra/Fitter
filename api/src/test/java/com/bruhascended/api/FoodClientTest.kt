package com.bruhascended.api

import com.bruhascended.api.models.food.FoodResponse
import com.bruhascended.api.models.foods.FoodsResponse
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FoodClientTest {

    @Test
    fun getFoods() {
        runBlocking {
            val response: FoodsResponse? = FoodClient.fdaApi.getFoods("Banana").body()
        }
    }

    @Test
    fun getFood() {
        runBlocking {
            val response: FoodResponse? = FoodClient.fdaApi.getFood().body()
        }
    }
}