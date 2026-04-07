package com.licensemanager.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubscriptionResponse(
    val success: Boolean,
    val subscription: SubscriptionData?,
    val message: String?
)

data class SubscriptionData(
    val id: Int,
    @SerializedName("pack_name") val packName: String?,
    @SerializedName("pack_sku") val packSku: String?,
    val price: Double?,
    val status: String?,
    @SerializedName("assigned_at") val assignedAt: String?,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("is_valid") val isValid: Boolean?
)

data class RequestSubscriptionBody(
    @SerializedName("pack_sku") val packSku: String
)

data class RequestSubscriptionResponse(
    val success: Boolean,
    val message: String?,
    val subscription: RequestedSubscriptionData?
)

data class RequestedSubscriptionData(
    val id: Int?,
    val status: String?,
    @SerializedName("requested_at") val requestedAt: String?
)

data class DeactivateResponse(
    val success: Boolean,
    val message: String?,
    @SerializedName("deactivated_at") val deactivatedAt: String?
)

data class SubscriptionHistoryResponse(
    val success: Boolean,
    val history: List<HistoryItem>?,
    val pagination: PaginationData?,
    val message: String?
)

data class HistoryItem(
    val id: Int,
    @SerializedName("pack_name") val packName: String?,
    val status: String?,
    @SerializedName("assigned_at") val assignedAt: String?,
    @SerializedName("expires_at") val expiresAt: String?
)

data class PaginationData(
    val page: Int,
    val limit: Int,
    val total: Int
)

data class PackListResponse(
    val success: Boolean,
    val data: List<PackItem>?,
    val message: String?
)

data class PackItem(
    val id: Int,
    val name: String,
    val description: String?,
    val sku: String,
    val price: Double,
    @SerializedName("validity_months") val validityMonths: Int
)
