package com.bruhascended.api

import com.bruhascended.api.services.FoodApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val base_url = "https://api.nal.usda.gov/fdc/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(base_url)
    .build()

object FoodClient {
    val fdaApi: FoodApi by lazy { retrofit.create(FoodApi::class.java) }
}