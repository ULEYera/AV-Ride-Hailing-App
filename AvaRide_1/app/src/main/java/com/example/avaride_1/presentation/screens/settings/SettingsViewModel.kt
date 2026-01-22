package com.example.avaride_1.presentation.screens.settings

import androidx.lifecycle.ViewModel
import com.example.avaride_1.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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

