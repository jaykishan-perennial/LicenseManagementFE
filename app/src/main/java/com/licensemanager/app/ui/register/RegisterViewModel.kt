package com.licensemanager.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.licensemanager.app.data.remote.dto.SignupResponse
import com.licensemanager.app.data.repository.AuthRepository
import com.licensemanager.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerState = MutableStateFlow<UiState<SignupResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<SignupResponse>> = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String, phone: String) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.signup(name, email, password, phone)
                _registerState.value = UiState.Success(response)
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterViewModel(authRepository) as T
        }
    }
}
