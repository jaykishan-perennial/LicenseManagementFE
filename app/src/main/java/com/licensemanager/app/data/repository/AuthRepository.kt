package com.licensemanager.app.data.repository

import com.licensemanager.app.data.remote.ApiService
import com.licensemanager.app.data.remote.dto.LoginRequest
import com.licensemanager.app.data.remote.dto.LoginResponse
import com.licensemanager.app.data.remote.dto.SignupRequest
import com.licensemanager.app.data.remote.dto.SignupResponse
import com.licensemanager.app.util.PreferenceManager

class AuthRepository(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    suspend fun login(email: String, password: String): LoginResponse {
        val response = apiService.login(LoginRequest(email, password))

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errorJson = com.google.gson.Gson().fromJson(
                    response.errorBody()?.string(),
                    Map::class.java
                )
                errorJson["message"] as? String ?: "Login failed"
            } catch (_: Exception) {
                "Login failed"
            }
            throw Exception(errorMsg)
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            throw Exception(body.message ?: "Login failed. Please check your credentials.")
        }

        body.apiKey?.let { preferenceManager.saveApiKey(it) }
        body.name?.let { preferenceManager.saveUserName(it) }
        body.phone?.let { preferenceManager.saveUserPhone(it) }

        return body
    }

    suspend fun signup(name: String, email: String, password: String, phone: String): SignupResponse {
        val response = apiService.signup(SignupRequest(name, email, password, phone))

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errorJson = com.google.gson.Gson().fromJson(
                    response.errorBody()?.string(),
                    Map::class.java
                )
                errorJson["message"] as? String ?: "Registration failed"
            } catch (_: Exception) {
                "Registration failed"
            }
            throw Exception(errorMsg)
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            throw Exception(body.message ?: "Registration failed")
        }

        body.apiKey?.let { preferenceManager.saveApiKey(it) }
        body.name?.let { preferenceManager.saveUserName(it) }
        body.phone?.let { preferenceManager.saveUserPhone(it) }

        return body
    }

    fun logout() {
        preferenceManager.clearAll()
    }

    val isLoggedIn: Boolean get() = preferenceManager.isLoggedIn
}
