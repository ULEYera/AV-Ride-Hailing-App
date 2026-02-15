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
    val remainingMinutes: Int = 1,
    val progress: Float = 0.0f, // 0.0 to 1.0
    val temperature: Int = 22,
    val lightingMode: LightingMode = LightingMode.WARM,
    val isPlaying: Boolean = false,
    val currentLat: Double = 1.3048,
    val currentLng: Double = 103.8318,
    val isRideComplete: Boolean = false,
    val routePoints: List<org.osmdroid.util.GeoPoint> = emptyList()
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

    private val osrmRepository = com.example.avaride_1.data.repository.OsrmRepository()

    fun startJourney() {
        // Run simulation in a coroutine
        // Total duration: 60 seconds (Slower ride)
        val durationSeconds = 60
        val updateIntervalMillis = 1000L // Update every second

        // Reset state
        _uiState.update { 
            it.copy(
                progress = 0f, 
                remainingMinutes = 1, // < 1 min
                currentLat = startLat,
                currentLng = startLng,
                routePoints = emptyList() // Clear previous route
            ) 
        }

        // Launch simulation
        val viewModelScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
        
        viewModelScope.launch {
            // 1. Fetch Route first
            val startPoint = org.osmdroid.util.GeoPoint(startLat, startLng)
            val endPoint = org.osmdroid.util.GeoPoint(endLat, endLng)
            val route = osrmRepository.fetchRoute(startPoint, endPoint)
            
            _uiState.update { it.copy(routePoints = route) }

            // 2. Simulate Movement along route
            var elapsedSeconds = 0
            
            // If route fetch failed, fallback to straight line (start -> end)
            val path = if (route.isNotEmpty()) route else listOf(startPoint, endPoint)
            val totalPathDistance = calculateTotalAuthDistance(path) // Rough approximation in indices or meters? 
            // For simplicity in demo: we will interpolate based on time purely across the index range of the list
            
            while (elapsedSeconds <= durationSeconds) {
                delay(updateIntervalMillis)
                elapsedSeconds++

                val progress = elapsedSeconds.toFloat() / durationSeconds
                val remainingSeconds = durationSeconds - elapsedSeconds
                val remainingMinutes = (remainingSeconds / 60) + if (remainingSeconds % 60 > 0) 1 else 0

                // Interpolate Position along the Path
                val currentPos = interpolatePath(path, progress)

                _uiState.update {
                    it.copy(
                        progress = progress,
                        remainingMinutes = remainingMinutes,
                        currentLat = currentPos.latitude,
                        currentLng = currentPos.longitude,
                        isRideComplete = elapsedSeconds >= durationSeconds
                    )
                }
            }

            // Ride Complete - Save Trip ONCE
            if (firestoreRepository != null) {
                 try {
                     val phoneNumber = userPrefs?.userPhoneNumber?.firstOrNull()
                     
                     if (!phoneNumber.isNullOrBlank()) {
                         val trip = com.example.avaride_1.domain.model.Trip(
                             id = java.util.UUID.randomUUID().toString(),
                             phoneNumber = phoneNumber,
                             destination = _uiState.value.destination,
                             pickup = "Current Location",
                             cost = 12.50,
                             distance = "5.2 km",
                             duration = "18 mins",
                             pickupLat = startLat,
                             pickupLng = startLng,
                             destLat = endLat,
                             destLng = endLng,
                             timestamp = System.currentTimeMillis()
                         )
                         firestoreRepository.saveTrip(trip)
                     }
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
            }
        }
    }

    private fun interpolatePath(path: List<org.osmdroid.util.GeoPoint>, progress: Float): org.osmdroid.util.GeoPoint {
        if (path.isEmpty()) return org.osmdroid.util.GeoPoint(0.0, 0.0)
        if (path.size == 1) return path.first()
        if (progress <= 0f) return path.first()
        if (progress >= 1f) return path.last()

        // Total segments
        val totalSegments = path.size - 1
        // Exact position in standard params
        val exactIndex = progress * totalSegments
        val index = exactIndex.toInt()
        val segmentProgress = exactIndex - index
        
        // Safety check
        if (index >= totalSegments) return path.last()

        val p1 = path[index]
        val p2 = path[index + 1]

        val lat = p1.latitude + (p2.latitude - p1.latitude) * segmentProgress
        val lng = p1.longitude + (p2.longitude - p1.longitude) * segmentProgress

        return org.osmdroid.util.GeoPoint(lat, lng)
    }

    // Unused helper, but good for future real distance calculation
    private fun calculateTotalAuthDistance(path: List<org.osmdroid.util.GeoPoint>): Double {
        return 0.0 
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

