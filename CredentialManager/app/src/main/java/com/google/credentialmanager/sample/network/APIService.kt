package com.google.credentialmanager.sample.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {
    @POST("register/start")
    fun registerStart(@Body authRegister: AuthRegister): Call<JsonObject>

    @POST("register/finish")
    fun registerFinish(@Body authRegister: AuthRegister): Call<JsonObject>

    @POST("login/start")
    fun loginStart(): Call<JsonObject>
}