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
    val destination: String = "Changi Airport",
    val remainingMinutes: Int = 18,
    val progress: Float = 0.0f, // 0.0 to 1.0
    val temperature: Int = 22,
    val lightingMode: LightingMode = LightingMode.WARM,
    val isPlaying: Boolean = false,
    val currentLat: Double = 1.3048,
    val currentLng: Double = 103.8318,
    val isRideComplete: Boolean = false,
    val routePoints: List<org.osmdroid.util.GeoPoint> = emptyList(),
    val currentSpeed: Double = 0.0 // km/h for display
)

// [PART 2] Vehicle Preferences (US-04)
// [PART 5] Inertial Sensors & AV Positioning (US-05)
class InRideViewModel(
    private val firestoreRepository: FirestoreRepository? = null,
    private val userPrefs: UserPreferencesRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(InRideUiState())
    val uiState: StateFlow<InRideUiState> = _uiState.asStateFlow()

    // Mock Coordinates: Orchard (Start) -> Changi Airport (End)
    // Real distance: ~25km, realistic drive time: ~18-25 minutes
    private val startLat = 1.3048
    private val startLng = 103.8318
    private val endLat = 1.3644
    private val endLng = 103.9915

    private val osrmRepository = com.example.avaride_1.data.repository.OsrmRepository()

    // Simulation parameters for realistic ride
    companion object {
        // For demo purposes, compress 18-minute ride into 3 minutes (180 seconds)
        // This gives a good balance between realism and demo experience
        private const val SIMULATED_RIDE_DURATION_SECONDS = 180 // 3 minutes demo
        private const val REAL_RIDE_DURATION_MINUTES = 18 // What we display to user
        private const val UPDATE_INTERVAL_MS = 500L // Smoother updates every 500ms
        private const val AVERAGE_SPEED_KMH = 50.0 // Realistic city/highway speed
    }

    // [PART 5] Integrate Sensors for AV Positioning (US-05)
    fun startJourney() {
        // Reset state
        _uiState.update { 
            it.copy(
                progress = 0f, 
                remainingMinutes = REAL_RIDE_DURATION_MINUTES,
                currentLat = startLat,
                currentLng = startLng,
                routePoints = emptyList(),
                isRideComplete = false,
                currentSpeed = 0.0
            )
        }

        viewModelScope.launch {
            // 1. Fetch Route first
            val startPoint = org.osmdroid.util.GeoPoint(startLat, startLng)
            val endPoint = org.osmdroid.util.GeoPoint(endLat, endLng)
            val route = osrmRepository.fetchRoute(startPoint, endPoint)
            
            _uiState.update { it.copy(routePoints = route) }

            // 2. Simulate Movement along route with realistic pacing
            val totalUpdates = (SIMULATED_RIDE_DURATION_SECONDS * 1000 / UPDATE_INTERVAL_MS).toInt()
            var currentUpdate = 0

            // If route fetch failed, fallback to straight line
            val path = if (route.isNotEmpty()) route else listOf(startPoint, endPoint)

            // Calculate total path distance for speed simulation
            val totalDistanceKm = calculatePathDistance(path)

            while (currentUpdate <= totalUpdates && !_uiState.value.isRideComplete) {
                delay(UPDATE_INTERVAL_MS)
                currentUpdate++

                // Calculate progress with slight randomization for realism
                val baseProgress = currentUpdate.toFloat() / totalUpdates
                // Add tiny variance to simulate real driving (traffic, acceleration, etc.)
                val variance = if (currentUpdate % 10 == 0) {
                    (Math.random() * 0.005 - 0.0025).toFloat() // ±0.25% variance occasionally
                } else 0f
                val progress = (baseProgress + variance).coerceIn(0f, 1f)

                // Calculate remaining time (display time, not simulation time)
                val remainingRealMinutes = ((1f - progress) * REAL_RIDE_DURATION_MINUTES).toInt()
                    .coerceAtLeast(0)

                // Interpolate Position along the Path with easing for smoother movement
                val easedProgress = easeInOutProgress(progress)
                val currentPos = interpolatePath(path, easedProgress)

                // Calculate simulated speed (varies slightly for realism)
                val speedVariance = 0.8 + Math.random() * 0.4 // 80% to 120% of average
                val currentSpeed = AVERAGE_SPEED_KMH * speedVariance

                val isComplete = progress >= 0.99f

                _uiState.update {
                    it.copy(
                        progress = progress,
                        remainingMinutes = if (isComplete) 0 else remainingRealMinutes.coerceAtLeast(1),
                        currentLat = currentPos.latitude,
                        currentLng = currentPos.longitude,
                        isRideComplete = isComplete,
                        currentSpeed = if (isComplete) 0.0 else currentSpeed
                    )
                }

                if (isComplete) break
            }

            // Ride Complete - Save Trip
            saveCompletedTrip()
        }
    }

    /**
     * Easing function for smoother movement (ease-in-out)
     * Makes the vehicle appear to accelerate and decelerate naturally
     */
    private fun easeInOutProgress(t: Float): Float {
        return if (t < 0.5f) {
            2 * t * t
        } else {
            1 - (-2 * t + 2).let { it * it } / 2
        }
    }

    private fun interpolatePath(path: List<org.osmdroid.util.GeoPoint>, progress: Float): org.osmdroid.util.GeoPoint {
        if (path.isEmpty()) return org.osmdroid.util.GeoPoint(startLat, startLng)
        if (path.size == 1) return path.first()
        if (progress <= 0f) return path.first()
        if (progress >= 1f) return path.last()

        val totalSegments = path.size - 1
        val exactIndex = progress * totalSegments
        val index = exactIndex.toInt().coerceIn(0, totalSegments - 1)
        val segmentProgress = exactIndex - index

        val p1 = path[index]
        val p2 = path[minOf(index + 1, path.size - 1)]

        // Linear interpolation between two points
        val lat = p1.latitude + (p2.latitude - p1.latitude) * segmentProgress
        val lng = p1.longitude + (p2.longitude - p1.longitude) * segmentProgress

        return org.osmdroid.util.GeoPoint(lat, lng)
    }

    /**
     * Calculate approximate path distance in kilometers using Haversine formula
     */
    private fun calculatePathDistance(path: List<org.osmdroid.util.GeoPoint>): Double {
        if (path.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until path.size - 1) {
            totalDistance += haversineDistance(
                path[i].latitude, path[i].longitude,
                path[i + 1].latitude, path[i + 1].longitude
            )
        }
        return totalDistance
    }

    /**
     * Haversine formula to calculate distance between two coordinates in km
     */
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private suspend fun saveCompletedTrip() {
        if (firestoreRepository != null) {
            try {
                val phoneNumber = userPrefs?.userPhoneNumber?.firstOrNull()

                if (!phoneNumber.isNullOrBlank()) {
                    val trip = com.example.avaride_1.domain.model.Trip(
                        id = java.util.UUID.randomUUID().toString(),
                        phoneNumber = phoneNumber,
                        destination = _uiState.value.destination,
                        pickup = "Orchard Road",
                        cost = 12.50,
                        distance = "25.2 km",
                        duration = "$REAL_RIDE_DURATION_MINUTES mins",
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

    fun updateTemperature(temp: Int) {
        _uiState.update { it.copy(temperature = temp.coerceIn(16, 28)) }
    }

    fun updateLighting(mode: LightingMode) {
        _uiState.update { it.copy(lightingMode = mode) }
    }

    fun toggleMusic() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun updateProgress(progress: Float) {
        // Manual override if needed
    }
}

