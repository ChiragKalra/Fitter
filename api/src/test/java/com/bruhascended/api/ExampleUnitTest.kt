package com.bruhascended.api

import com.bruhascended.api.services.FoodApi
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun fetchFood(){
        runBlocking {
            val response = FoodClient.fdaApi.getFoodsv2("banana").body()
        }
    }
}