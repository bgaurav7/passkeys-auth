package com.google.credentialmanager.sample.network

data class AuthLogin(
    val username: String,
    val timeout: Int,
    val userVerification: String,
    val rpId: String,
)