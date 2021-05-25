package com.example.api

import org.junit.Test

class FoodClientTest {
    private val foodClient = FoodClient()

    @Test
    fun getFoods() {
        val foods = foodClient.api.getFoods(query = "paratha").execute()
    }

    @Test
    fun getFood(){
        val food = foodClient.api.getFood().execute()
    }
}