package com.example.avaride_1.presentation.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.avaride_1.data.nfc.AvaRideHceService
import com.example.avaride_1.data.nfc.NFCManager
import com.example.avaride_1.data.nfc.NFCManagerState
import com.example.avaride_1.data.nfc.NFCStatus
import com.example.avaride_1.data.repository.NFCSessionRepository
import com.example.avaride_1.domain.model.NFCErrorCode
import com.example.avaride_1.domain.model.NFCSession
import com.example.avaride_1.domain.model.NFCUnlockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for NFC unlock functionality.
 * Manages NFC session lifecycle, reader mode, and HCE service coordination.
 */
class NFCUnlockViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "NFCUnlockViewModel"
    }

    private val nfcManager = NFCManager(application)
    private val sessionRepository = NFCSessionRepository()

    // UI State
    private val _uiState = MutableStateFlow<NFCUnlockUiState>(NFCUnlockUiState.Initial)
    val uiState: StateFlow<NFCUnlockUiState> = _uiState.asStateFlow()

    // Current session
    private val _currentSession = MutableStateFlow<NFCSession?>(null)
    val currentSession: StateFlow<NFCSession?> = _currentSession.asStateFlow()

    // Vehicle info for display
    private val _vehicleInfo = MutableStateFlow<VehicleDisplayInfo?>(null)
    val vehicleInfo: StateFlow<VehicleDisplayInfo?> = _vehicleInfo.asStateFlow()

    init {
        // Observe NFC manager state changes
        viewModelScope.launch {
            nfcManager.nfcState.collect { state ->
                handleNfcManagerState(state)
            }
        }
    }

    /**
     * Initialize NFC unlock for a trip
     */
    fun initializeUnlock(
        tripId: String,
        vehicleId: String,
        userId: String,
        vehicleName: String = "AvaRide Vehicle",
        vehiclePlate: String = ""
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Initializing unlock for trip: $tripId, vehicle: $vehicleId")

            // Set vehicle display info
            _vehicleInfo.value = VehicleDisplayInfo(
                id = vehicleId,
                name = vehicleName,
                licensePlate = vehiclePlate
            )

            // Check NFC status
            when (nfcManager.getNfcStatus()) {
                NFCStatus.NOT_AVAILABLE -> {
                    _uiState.value = NFCUnlockUiState.NfcUnavailable(
                        message = "NFC is not available on this device"
                    )
                    return@launch
                }
                NFCStatus.DISABLED -> {
                    _uiState.value = NFCUnlockUiState.NfcDisabled(
                        message = "Please enable NFC in settings"
                    )
                    return@launch
                }
                NFCStatus.ENABLED -> {
                    // Continue with session creation
                }
            }

            try {
                // Create session in repository (Firebase)
                val session = sessionRepository.createSession(
                    tripId = tripId,
                    vehicleId = vehicleId,
                    userId = userId
                )

                _currentSession.value = session

                // Set session for HCE service
                AvaRideHceService.setSession(session)

                // Update local NFC manager
                nfcManager.startSession(tripId, vehicleId, userId)

                _uiState.value = NFCUnlockUiState.ReadyToScan(
                    sessionId = session.sessionId,
                    expiresAt = session.expiresAt
                )

                Log.d(TAG, "Session created: ${session.sessionId}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to create session", e)
                _uiState.value = NFCUnlockUiState.Error(
                    code = NFCErrorCode.NETWORK_ERROR,
                    message = "Failed to initialize unlock: ${e.message}"
                )
            }
        }
    }

    /**
     * Start NFC reader mode (phone as reader)
     */
    fun startReaderMode(activity: Activity) {
        Log.d(TAG, "Starting reader mode")

        nfcManager.enableReaderMode(activity) { result ->
            viewModelScope.launch {
                handleUnlockResult(result)
            }
        }

        _uiState.value = NFCUnlockUiState.Scanning
    }

    /**
     * Stop NFC reader mode
     */
    fun stopReaderMode(activity: Activity) {
        Log.d(TAG, "Stopping reader mode")
        nfcManager.disableReaderMode(activity)
    }

    /**
     * Handle NFC manager state changes
     */
    private fun handleNfcManagerState(state: NFCManagerState) {
        when (state) {
            is NFCManagerState.Idle -> {
                // No action needed
            }
            is NFCManagerState.WaitingForTap -> {
                _uiState.value = NFCUnlockUiState.ReadyToScan(
                    sessionId = state.session.sessionId,
                    expiresAt = state.session.expiresAt
                )
            }
            is NFCManagerState.Scanning -> {
                _uiState.value = NFCUnlockUiState.Scanning
            }
            is NFCManagerState.Processing -> {
                _uiState.value = NFCUnlockUiState.Processing
            }
            is NFCManagerState.Success -> {
                _uiState.value = NFCUnlockUiState.Success(
                    vehicleId = state.vehicleId
                )
            }
            is NFCManagerState.Error -> {
                _uiState.value = NFCUnlockUiState.Error(
                    code = NFCErrorCode.UNKNOWN_ERROR,
                    message = state.message
                )
            }
        }
    }

    /**
     * Handle unlock result
     */
    private suspend fun handleUnlockResult(result: NFCUnlockResult) {
        when (result) {
            is NFCUnlockResult.Success -> {
                Log.d(TAG, "Unlock successful: ${result.vehicleId}")

                // Update session in repository
                _currentSession.value?.let { session ->
                    sessionRepository.completeSession(session.sessionId, result.vehicleId)
                }

                _uiState.value = NFCUnlockUiState.Success(
                    vehicleId = result.vehicleId
                )
            }
            is NFCUnlockResult.Error -> {
                Log.e(TAG, "Unlock failed: ${result.code} - ${result.message}")

                // Update session in repository
                _currentSession.value?.let { session ->
                    sessionRepository.failSession(session.sessionId, result.message)
                }

                _uiState.value = NFCUnlockUiState.Error(
                    code = result.code,
                    message = result.message
                )
            }
        }
    }

    /**
     * Retry unlock after error
     */
    fun retryUnlock() {
        val session = _currentSession.value
        val vehicle = _vehicleInfo.value

        if (session != null && vehicle != null && !session.isExpired()) {
            _uiState.value = NFCUnlockUiState.ReadyToScan(
                sessionId = session.sessionId,
                expiresAt = session.expiresAt
            )
        } else {
            // Session expired, need to reinitialize
            _uiState.value = NFCUnlockUiState.Error(
                code = NFCErrorCode.SESSION_EXPIRED,
                message = "Session expired. Please try booking again."
            )
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup(activity: Activity) {
        stopReaderMode(activity)
        nfcManager.clearSession()
        AvaRideHceService.setSession(null)
        _currentSession.value = null
    }

    override fun onCleared() {
        super.onCleared()
        nfcManager.clearSession()
        AvaRideHceService.setSession(null)
    }
}

/**
 * UI State for NFC Unlock screen
 */
sealed class NFCUnlockUiState {
    object Initial : NFCUnlockUiState()

    data class ReadyToScan(
        val sessionId: String,
        val expiresAt: Long
    ) : NFCUnlockUiState()

    object Scanning : NFCUnlockUiState()

    object Processing : NFCUnlockUiState()

    data class Success(
        val vehicleId: String
    ) : NFCUnlockUiState()

    data class Error(
        val code: NFCErrorCode,
        val message: String
    ) : NFCUnlockUiState()

    data class NfcUnavailable(
        val message: String
    ) : NFCUnlockUiState()

    data class NfcDisabled(
        val message: String
    ) : NFCUnlockUiState()
}

/**
 * Vehicle display info for UI
 */
data class VehicleDisplayInfo(
    val id: String,
    val name: String,
    val licensePlate: String
)

