package com.bruhascended.fitapp.data

import com.example.api.FoodClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

object FdaApi {
    fun getFoods(){
        CoroutineScope(IO).launch {
            FoodClient.fdaApi.getFoods("Banana")
        }
    }
}