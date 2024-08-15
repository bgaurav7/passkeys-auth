package com.google.credentialmanager.sample.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
    @POST("register/start")
    fun registerStart(@Body authModel: AuthModel): Call<JsonObject>

    @POST("register/finish")
    fun registerFinish(@Body authModel: AuthModel): Call<JsonObject>

    @POST("login/start")
    fun loginStart(@Body authModel: AuthModel): Call<JsonObject>

    @POST("login/finish")
    fun loginFinish(@Body authModel: AuthModel): Call<JsonObject>
}