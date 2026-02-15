package com.example.avaride_1.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avaride_1.data.repository.FirestoreRepository
import com.example.avaride_1.data.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val name: String = "", // New
    val isOtpSent: Boolean = false,
    val isNewUser: Boolean = false, // New
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, error = null) }
    }

    fun onOtpCodeChange(code: String) {
        if (code.length <= 4) {
            _uiState.update { it.copy(otpCode = code, error = null) }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun sendOtp() {
        val phone = _uiState.value.phoneNumber
        if (phone.isBlank() || phone.length < 8) {
            _uiState.update { it.copy(error = "Please enter a valid phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1500) // Simulate network call
            _uiState.update { it.copy(isLoading = false, isOtpSent = true) }
        }
    }

    fun verifyOtp() {
        val otp = _uiState.value.otpCode
        if (otp.length != 4) {
            _uiState.update { it.copy(error = "Enter a 4-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val phone = _uiState.value.phoneNumber

            try {
                // Check if user exists in Firestore
                val user = firestoreRepository.getUser(phone)
                
                if (user != null) {
                    // User exists, log in
                    userPreferencesRepository.saveUser(phone)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    // User doesn't exist, prompt for name
                    _uiState.update { it.copy(isLoading = false, isNewUser = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Login failed: ${e.message}") }
            }
        }
    }

    fun completeRegistration() {
        val name = _uiState.value.name
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val phone = _uiState.value.phoneNumber

            try {
                firestoreRepository.createUser(phone, name)
                userPreferencesRepository.saveUser(phone)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Registration failed: ${e.message}") }
            }
        }
    }
}
