package com.licensemanager.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.licensemanager.app.data.remote.dto.HistoryItem
import com.licensemanager.app.data.repository.SubscriptionRepository
import com.licensemanager.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val sortOrder: String = "desc"
)

class SubscriptionHistoryViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var totalItems = 0

    fun loadHistory(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            _uiState.value = _uiState.value.copy(items = emptyList(), hasMore = true, error = null)
        }

        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val response = subscriptionRepository.getHistory(
                    page = currentPage,
                    limit = Constants.DEFAULT_PAGE_SIZE,
                    sort = _uiState.value.sortOrder
                )

                val newItems = response.history ?: emptyList()
                totalItems = response.pagination?.total ?: 0

                val allItems = if (currentPage == 1) newItems
                else _uiState.value.items + newItems

                _uiState.value = _uiState.value.copy(
                    items = allItems,
                    isLoading = false,
                    hasMore = allItems.size < totalItems
                )
                currentPage++
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load history"
                )
            }
        }
    }

    fun toggleSort() {
        val newSort = if (_uiState.value.sortOrder == "desc") "asc" else "desc"
        _uiState.value = _uiState.value.copy(sortOrder = newSort)
        loadHistory(reset = true)
    }

    class Factory(private val subscriptionRepository: SubscriptionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubscriptionHistoryViewModel(subscriptionRepository) as T
        }
    }
}
