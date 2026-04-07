package com.licensemanager.app.data.repository

import com.licensemanager.app.data.remote.ApiService
import com.licensemanager.app.data.remote.dto.DeactivateResponse
import com.licensemanager.app.data.remote.dto.PackItem
import com.licensemanager.app.data.remote.dto.RequestSubscriptionBody
import com.licensemanager.app.data.remote.dto.RequestSubscriptionResponse
import com.licensemanager.app.data.remote.dto.SubscriptionData
import com.licensemanager.app.data.remote.dto.SubscriptionHistoryResponse

class SubscriptionRepository(private val apiService: ApiService) {

    suspend fun getCurrentSubscription(): SubscriptionData? {
        val response = apiService.getSubscription()

        if (response.code() == 404) return null

        if (!response.isSuccessful) {
            throw Exception(parseError(response.errorBody()?.string(), "Failed to load subscription"))
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            if (body.message?.contains("No active", ignoreCase = true) == true) return null
            throw Exception(body.message ?: "Failed to load subscription")
        }

        return body.subscription
    }

    suspend fun getAvailablePacks(): List<PackItem> {
        val response = apiService.getSubscriptionPacks()

        if (!response.isSuccessful) {
            throw Exception(parseError(response.errorBody()?.string(), "Failed to load packs"))
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            throw Exception(body.message ?: "Failed to load packs")
        }

        return body.data ?: emptyList()
    }

    suspend fun requestSubscription(packSku: String): RequestSubscriptionResponse {
        val response = apiService.requestSubscription(RequestSubscriptionBody(packSku))

        if (!response.isSuccessful) {
            throw Exception(parseError(response.errorBody()?.string(), "Failed to request subscription"))
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            throw Exception(body.message ?: "Failed to request subscription")
        }

        return body
    }

    suspend fun deactivateSubscription(): DeactivateResponse {
        val response = apiService.deactivateSubscription()

        if (!response.isSuccessful) {
            throw Exception(parseError(response.errorBody()?.string(), "Failed to deactivate subscription"))
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            throw Exception(body.message ?: "Failed to deactivate subscription")
        }

        return body
    }

    suspend fun getHistory(page: Int, limit: Int, sort: String): SubscriptionHistoryResponse {
        val response = apiService.getSubscriptionHistory(page, limit, sort)

        if (!response.isSuccessful) {
            throw Exception(parseError(response.errorBody()?.string(), "Failed to load subscription history"))
        }

        val body = response.body() ?: throw Exception("Empty response from server")

        if (!body.success) {
            throw Exception(body.message ?: "Failed to load subscription history")
        }

        return body
    }

    private fun parseError(errorBody: String?, fallback: String): String {
        return try {
            val errorJson = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
            errorJson["message"] as? String ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
