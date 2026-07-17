package com.littlenest.nursery.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.littlenest.nursery.ui.common.AppConfig

object RetrofitClient {

    // use http://10.0.2.2:3000/ for localhost in Android Emulator
    //private const val BASE_URL = ""

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
