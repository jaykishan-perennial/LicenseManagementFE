package com.licensemanager.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.licensemanager.app.data.remote.dto.DeactivateResponse
import com.licensemanager.app.data.remote.dto.SubscriptionData
import com.licensemanager.app.data.repository.SubscriptionRepository
import com.licensemanager.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val subscriptionRepository: SubscriptionRepository) : ViewModel() {

    private val _subscriptionState = MutableStateFlow<UiState<SubscriptionData?>>(UiState.Loading)
    val subscriptionState: StateFlow<UiState<SubscriptionData?>> = _subscriptionState.asStateFlow()

    private val _deactivateEvent = MutableSharedFlow<Result<DeactivateResponse>>()
    val deactivateEvent: SharedFlow<Result<DeactivateResponse>> = _deactivateEvent.asSharedFlow()

    fun loadSubscription() {
        _subscriptionState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val subscription = subscriptionRepository.getCurrentSubscription()
                _subscriptionState.value = UiState.Success(subscription)
            } catch (e: Exception) {
                _subscriptionState.value = UiState.Error(e.message ?: "Failed to load subscription")
            }
        }
    }

    fun deactivateSubscription() {
        viewModelScope.launch {
            try {
                val result = subscriptionRepository.deactivateSubscription()
                _deactivateEvent.emit(Result.success(result))
                loadSubscription()
            } catch (e: Exception) {
                _deactivateEvent.emit(Result.failure(e))
            }
        }
    }

    class Factory(private val subscriptionRepository: SubscriptionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(subscriptionRepository) as T
        }
    }
}
