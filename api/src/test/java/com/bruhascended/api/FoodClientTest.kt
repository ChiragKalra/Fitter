package com.bruhascended.api

import com.bruhascended.api.models.foodsv2.Foodsv2Response
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FoodClientTest {

    @Test
    fun getFoodv2() {
        runBlocking {
            val response: Foodsv2Response? = FoodClient.fdaApi.getFoodsv2("Banana").body()
        }
    }
}