package com.example.avaride_1.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avaride_1.data.remote.GeminiPredictiveService
import com.example.avaride_1.domain.model.Destination
import com.example.avaride_1.domain.model.Location
import com.example.avaride_1.domain.model.UserContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Prediction(
        val destination: Destination,
        val etaMinutes: Int,
        val estimatedPrice: Double
    ) : HomeUiState()
    object NoPrediction : HomeUiState() // Show manual input when AI can't predict
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val geminiService: GeminiPredictiveService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPrediction()
    }

    fun loadPrediction() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                // Get user context (in production, this would come from location services)
                val context = getCurrentUserContext()

                // Call Gemini API for prediction
                val predictedDestination = geminiService.predictDestination(context)

                if (predictedDestination != null) {
                    _uiState.value = HomeUiState.Prediction(
                        destination = predictedDestination,
                        etaMinutes = 4, // Mock data - would calculate from location
                        estimatedPrice = 12.50 // Mock data - would get from pricing service
                    )
                } else {
                    // No prediction available - show manual input
                    _uiState.value = HomeUiState.NoPrediction
                }
            } catch (e: Exception) {
                // On error, also show manual input (graceful fallback)
                println("HomeViewModel: Prediction error - ${e.message}, showing manual input")
                _uiState.value = HomeUiState.NoPrediction
            }
        }
    }

    private fun getCurrentUserContext(): UserContext {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Simulate realistic Singapore locations
        // Current location: User is somewhere in Singapore
        val currentLocation = Location(
            latitude = 1.3521,
            longitude = 103.8198,
            address = "Orchard Road, Singapore"
        )

        // Home: Singapore 531174 (Punggol area)
        val homeDestination = Destination(
            name = "Home",
            address = "Blk 174 Punggol Field, Singapore 531174",
            latitude = 1.4010,
            longitude = 103.9070,
            confidence = 0.95f
        )

        // Work: SIT Punggol Campus
        val workDestination = Destination(
            name = "Work",
            address = "SIT Punggol Campus, 10 Dover Drive, Singapore 138683",
            latitude = 1.4086,
            longitude = 103.9046,
            confidence = 0.92f
        )

        // Additional common destinations to make predictions more interesting
        val mallDestination = Destination(
            name = "Waterway Point",
            address = "83 Punggol Central, Singapore 828761",
            latitude = 1.4062,
            longitude = 103.9022,
            confidence = 0.75f
        )

        // Build realistic travel history based on time of day
        val recentDestinations = when (currentHour) {
            in 6..9 -> listOf(homeDestination, workDestination, mallDestination) // Morning: likely going to work
            in 17..20 -> listOf(workDestination, homeDestination, mallDestination) // Evening: likely going home
            in 12..14 -> listOf(workDestination, mallDestination, homeDestination) // Lunch: maybe mall
            else -> listOf(homeDestination, workDestination, mallDestination) // Default
        }

        return UserContext(
            currentLocation = currentLocation,
            currentTime = System.currentTimeMillis(),
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
            hourOfDay = currentHour,
            recentDestinations = recentDestinations
        )
    }
}

