package com.example.avaride_1.presentation.screens.inride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avaride_1.domain.model.LightingMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import com.example.avaride_1.data.repository.FirestoreRepository
import com.example.avaride_1.data.repository.UserPreferencesRepository

data class InRideUiState(
    val destination: String = "Home",
    val remainingMinutes: Int = 2,
    val progress: Float = 0.0f, // 0.0 to 1.0
    val temperature: Int = 22,
    val lightingMode: LightingMode = LightingMode.WARM,
    val isPlaying: Boolean = false,
    val currentLat: Double = 1.3048,
    val currentLng: Double = 103.8318,
    val isRideComplete: Boolean = false
)

class InRideViewModel(
    private val firestoreRepository: FirestoreRepository? = null,
    private val userPrefs: UserPreferencesRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(InRideUiState())
    val uiState: StateFlow<InRideUiState> = _uiState.asStateFlow()

    // Mock Coordinates: Orchard (Start) -> Changi (End)
    private val startLat = 1.3048
    private val startLng = 103.8318
    private val endLat = 1.3644
    private val endLng = 103.9915

    fun startJourney() {
        // Run simulation in a coroutine
        // Total duration: 120 seconds (2 minutes)
        val durationSeconds = 120
        val updateIntervalMillis = 1000L // Update every second

        // Reset state
        _uiState.update { 
            it.copy(
                progress = 0f, 
                remainingMinutes = 2,
                currentLat = startLat,
                currentLng = startLng
            ) 
        }

        // Launch simulation
        // In a real app, this would be a repository flow or worker
        // Using viewModelScope for demo simplicity
        val viewModelScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main) // using main for immediate updates in demo
        
        viewModelScope.launch {
            var elapsedSeconds = 0
            while (elapsedSeconds <= durationSeconds) {
                delay(updateIntervalMillis)
                elapsedSeconds++

                val progress = elapsedSeconds.toFloat() / durationSeconds
                val remainingSeconds = durationSeconds - elapsedSeconds
                val remainingMinutes = (remainingSeconds / 60) + if (remainingSeconds % 60 > 0) 1 else 0

                // Linear Interpolation for coordinates
                val currentLat = startLat + (endLat - startLat) * progress
                val currentLng = startLng + (endLng - startLng) * progress

                            // Save trip to Firestore
                            val phoneNumber = userPrefs?.userPhoneNumber?.firstOrNull()
                            if (!phoneNumber.isNullOrBlank() && firestoreRepository != null) {
                                // Launch in global scope or similar if ViewModel scope is cancelled, but here we just launch in existing scope
                                launch {
                                     try {
                                         val trip = com.example.avaride_1.domain.model.Trip(
                                             id = java.util.UUID.randomUUID().toString(),
                                             phoneNumber = phoneNumber,
                                             destination = _uiState.value.destination,
                                             pickup = "Current Location", // Simplified
                                             cost = 12.50,
                                             distance = "5.2 km",
                                             duration = "18 mins",
                                             pickupLat = startLat,
                                             pickupLng = startLng,
                                             destLat = endLat,
                                             destLng = endLng
                                         )
                                         firestoreRepository.saveTrip(trip)
                                     } catch (e: Exception) {
                                         e.printStackTrace() // Log error
                                     }
                                }
                            }

                _uiState.update {
                    it.copy(
                        progress = progress,
                        remainingMinutes = remainingMinutes,
                        currentLat = currentLat,
                        currentLng = currentLng,
                        isRideComplete = elapsedSeconds >= durationSeconds
                    )
                }
            }
        }
    }

    fun updateTemperature(temp: Int) {
        _uiState.update { it.copy(temperature = temp.coerceIn(16, 28)) }
    }

    fun updateLighting(mode: LightingMode) {
        _uiState.update { it.copy(lightingMode = mode) }
    }

    fun toggleMusic() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    // Legacy method kept for compatibility if needed, but startJourney replaces it
    fun updateProgress(progress: Float) {
        // No-op or manual override
    }
}

