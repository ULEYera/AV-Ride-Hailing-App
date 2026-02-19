package com.example.avaride_1.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avaride_1.data.repository.FirestoreRepository
import com.example.avaride_1.data.repository.UserPreferencesRepository
import com.example.avaride_1.domain.model.PaymentMethod
import com.example.avaride_1.domain.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "John Doe",
    val paymentMethod: PaymentMethod = PaymentMethod.GOOGLE_PAY,
    val rideCount: Int = 0,
    val homeLocation: String = "",
    val workLocation: String = "",
    val rideHistory: List<Trip> = emptyList(),
    val isLoadingHistory: Boolean = false
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

    private var currentPhoneNumber: String? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userPrefs?.userPhoneNumber?.collect { phoneNumber ->
                currentPhoneNumber = phoneNumber
                if (!phoneNumber.isNullOrBlank() && firestoreRepository != null) {
                    fetchUserData(phoneNumber)
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val phone = currentPhoneNumber ?: userPrefs?.userPhoneNumber?.firstOrNull()
            if (!phone.isNullOrBlank() && firestoreRepository != null) {
                fetchUserData(phone)
            }
        }
    }

    private suspend fun fetchUserData(phoneNumber: String) {
        try {
            val user = firestoreRepository?.getUser(phoneNumber)
            if (user != null) {
                 _uiState.update { 
                     it.copy(
                         userName = user.name,
                         homeLocation = user.homeLocation ?: "",
                         workLocation = user.workLocation ?: "",
                         paymentMethod = if (user.paymentMethod != null) PaymentMethod.valueOf(user.paymentMethod) else PaymentMethod.GOOGLE_PAY
                     ) 
                 }
                 loadRideHistory(phoneNumber)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadRideHistory(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true) }
            val history = firestoreRepository?.getTripHistory(phoneNumber) ?: emptyList()
            _uiState.update { 
                it.copy(
                    rideHistory = history,
                    rideCount = history.size,
                    isLoadingHistory = false
                ) 
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPrefs?.clearUser()
        }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
        viewModelScope.launch {
             val phone = userPrefs?.userPhoneNumber?.firstOrNull()
             if (!phone.isNullOrBlank()) {
                 firestoreRepository?.updatePaymentMethod(phone, method.name)
             }
        }
    }
    
    fun updateLocation(type: String, address: String) {
        if (type == "Home") {
            _uiState.update { it.copy(homeLocation = address) }
        } else {
            _uiState.update { it.copy(workLocation = address) }
        }
        
        viewModelScope.launch {
            val phone = userPrefs?.userPhoneNumber?.firstOrNull()
            if (!phone.isNullOrBlank()) {
                firestoreRepository?.updateUserLocation(phone, type, address)
            }
        }
    }
}

