package com.example.avaride_1.domain.model

data class Ride(
    val id: String,
    val destination: Destination,
    val vehicle: Vehicle,
    val pickupLocation: Location,
    val estimatedPrice: Double,
    val status: RideStatus = RideStatus.REQUESTED,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val actualPrice: Double? = null,
    val distanceKm: Double = 0.0,
    val currentProgress: Float = 0.0f // 0.0 to 1.0
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

enum class RideStatus {
    REQUESTED,
    VEHICLE_ASSIGNED,
    VEHICLE_ARRIVING,
    READY_TO_UNLOCK,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

