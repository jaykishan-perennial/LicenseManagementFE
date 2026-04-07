package com.licensemanager.app.ui.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.licensemanager.app.data.remote.dto.PackItem
import com.licensemanager.app.data.remote.dto.RequestSubscriptionResponse
import com.licensemanager.app.data.repository.SubscriptionRepository
import com.licensemanager.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestSubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _packsState = MutableStateFlow<UiState<List<PackItem>>>(UiState.Idle)
    val packsState: StateFlow<UiState<List<PackItem>>> = _packsState.asStateFlow()

    private val _requestState = MutableStateFlow<UiState<RequestSubscriptionResponse>>(UiState.Idle)
    val requestState: StateFlow<UiState<RequestSubscriptionResponse>> = _requestState.asStateFlow()

    private val _selectedPack = MutableStateFlow<PackItem?>(null)
    val selectedPack: StateFlow<PackItem?> = _selectedPack.asStateFlow()

    private var allPacks: List<PackItem> = emptyList()

    init {
        loadPacks()
    }

    fun loadPacks() {
        _packsState.value = UiState.Loading
        viewModelScope.launch {
            try {
                allPacks = subscriptionRepository.getAvailablePacks()
                _packsState.value = UiState.Success(allPacks)
            } catch (e: Exception) {
                _packsState.value = UiState.Error(e.message ?: "Failed to load packs")
            }
        }
    }

    fun filterPacks(query: String) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            _packsState.value = UiState.Success(allPacks)
            return
        }
        val filtered = allPacks.filter {
            it.name.lowercase().contains(q) ||
                    it.sku.lowercase().contains(q) ||
                    (it.description?.lowercase()?.contains(q) == true)
        }
        _packsState.value = UiState.Success(filtered)
    }

    fun selectPack(pack: PackItem) {
        _selectedPack.value = pack
    }

    fun clearSelection() {
        _selectedPack.value = null
    }

    fun requestSubscription() {
        val pack = _selectedPack.value
        if (pack == null) {
            _requestState.value = UiState.Error("Please select a subscription pack")
            return
        }

        _requestState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val response = subscriptionRepository.requestSubscription(pack.sku)
                _requestState.value = UiState.Success(response)
            } catch (e: Exception) {
                _requestState.value = UiState.Error(e.message ?: "Failed to request subscription")
            }
        }
    }

    fun resetState() {
        _requestState.value = UiState.Idle
    }

    class Factory(private val subscriptionRepository: SubscriptionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RequestSubscriptionViewModel(subscriptionRepository) as T
        }
    }
}
