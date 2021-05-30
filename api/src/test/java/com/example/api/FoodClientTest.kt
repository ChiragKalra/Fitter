package com.example.api

import com.example.api.models.food.FoodResponse
import com.example.api.models.foods.FoodsResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FoodClientTest {

    @Test
    fun getFoods(){
        CoroutineScope(IO).launch{
            val foods:FoodsResponse = FoodClient.fdaApi.getFoods("Banana")
        }
    }

    @Test
    fun getFood(){
        CoroutineScope(IO).launch{
            val foods: FoodResponse = FoodClient.fdaApi.getFood()
        }
    }
}