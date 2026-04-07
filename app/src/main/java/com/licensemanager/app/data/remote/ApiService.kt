package com.licensemanager.app.data.remote

import com.licensemanager.app.data.remote.dto.DeactivateResponse
import com.licensemanager.app.data.remote.dto.LoginRequest
import com.licensemanager.app.data.remote.dto.LoginResponse
import com.licensemanager.app.data.remote.dto.PackListResponse
import com.licensemanager.app.data.remote.dto.SignupRequest
import com.licensemanager.app.data.remote.dto.SignupResponse
import com.licensemanager.app.data.remote.dto.RequestSubscriptionBody
import com.licensemanager.app.data.remote.dto.RequestSubscriptionResponse
import com.licensemanager.app.data.remote.dto.SubscriptionHistoryResponse
import com.licensemanager.app.data.remote.dto.SubscriptionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("sdk/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("sdk/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @GET("sdk/v1/subscription-packs")
    suspend fun getSubscriptionPacks(
        @Query("search") search: String? = null
    ): Response<PackListResponse>

    @GET("sdk/v1/subscription")
    suspend fun getSubscription(): Response<SubscriptionResponse>

    @POST("sdk/v1/subscription")
    suspend fun requestSubscription(@Body request: RequestSubscriptionBody): Response<RequestSubscriptionResponse>

    @DELETE("sdk/v1/subscription")
    suspend fun deactivateSubscription(): Response<DeactivateResponse>

    @GET("sdk/v1/subscription-history")
    suspend fun getSubscriptionHistory(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("sort") sort: String
    ): Response<SubscriptionHistoryResponse>
}
