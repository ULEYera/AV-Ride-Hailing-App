package com.example.avaride_1.domain.model

data class Trip(
    val id: String = "",
    val phoneNumber: String = "",
    val destination: String = "",
    val pickup: String = "",
    val cost: Double = 0.0,
    val distance: String = "",
    val duration: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val destLat: Double = 0.0,
    val destLng: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
