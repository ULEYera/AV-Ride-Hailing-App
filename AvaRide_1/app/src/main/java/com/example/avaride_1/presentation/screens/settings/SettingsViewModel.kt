package com.example.avaride_1.presentation.screens.settings

import androidx.lifecycle.ViewModel
import com.example.avaride_1.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import androidx.lifecycle.viewModelScope
import com.example.avaride_1.data.repository.FirestoreRepository
import com.example.avaride_1.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "John Doe",
    val quietMode: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.GOOGLE_PAY,
    val rideCount: Int = 42,
    val preferences: Map<String, Boolean> = mapOf(
        "notifications" to true,
        "location_services" to true,
        "accessibility_mode" to false
    )
)

class SettingsViewModel(
    private val firestoreRepository: FirestoreRepository? = null,
    private val userPrefs: UserPreferencesRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val phoneNumber = userPrefs?.userPhoneNumber?.firstOrNull()
                if (!phoneNumber.isNullOrBlank() && firestoreRepository != null) {
                    val user = firestoreRepository.getUser(phoneNumber)
                    if (user != null && user.name.isNotBlank()) {
                         _uiState.update { it.copy(userName = user.name) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPrefs?.clearUser()
        }
    }

    fun toggleQuietMode() {
        _uiState.update { it.copy(quietMode = !it.quietMode) }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun updatePreference(key: String, value: Boolean) {
        _uiState.update {
            it.copy(preferences = it.preferences.toMutableMap().apply {
                put(key, value)
            })
        }
    }
}

