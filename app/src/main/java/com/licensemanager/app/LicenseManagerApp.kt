package com.licensemanager.app

import android.app.Application
import com.licensemanager.app.data.remote.ApiClient
import com.licensemanager.app.data.repository.AuthRepository
import com.licensemanager.app.data.repository.SubscriptionRepository
import com.licensemanager.app.util.PreferenceManager

class LicenseManagerApp : Application() {

    lateinit var preferenceManager: PreferenceManager
        private set

    private val apiClient: ApiClient by lazy { ApiClient(preferenceManager) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiClient.apiService, preferenceManager)
    }

    val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepository(apiClient.apiService)
    }

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
    }
}
