package com.bruhascended.api

import com.bruhascended.api.services.FoodApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val base_urlv2 = "https://api.edamam.com/api/food-database/v2/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(base_urlv2)
    .build()

object FoodClient {
    val fdaApi: FoodApi by lazy { retrofit.create(FoodApi::class.java) }
}