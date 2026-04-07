package com.licensemanager.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    @SerializedName("api_key") val apiKey: String?,
    val name: String?,
    val phone: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    val message: String?
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

data class SignupResponse(
    val success: Boolean,
    @SerializedName("api_key") val apiKey: String?,
    val name: String?,
    val phone: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    val message: String?
)
