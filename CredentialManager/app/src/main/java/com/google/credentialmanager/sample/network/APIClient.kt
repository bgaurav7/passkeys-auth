package com.google.credentialmanager.sample.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//private const val BASE_URL = "http://homelab.bgaurav.in:3300/"

object RetrofitClient {
    private const val BASE_URL = "https://rapid-charming-vulture.ngrok-free.app/" //""http://homelab.bgaurav.in:3300/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object APIClient {
    val apiService: APIService by lazy {
        RetrofitClient.retrofit.create(APIService::class.java)
    }
}