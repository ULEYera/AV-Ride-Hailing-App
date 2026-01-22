package com.example.avaride_1.domain.model

data class Vehicle(
    val id: String,
    val model: String,
    val type: VehicleType,
    val latitude: Double,
    val longitude: Double,
    val etaMinutes: Int,
    val licensePlate: String,
    val color: String = "White",
    val batteryLevel: Int = 100
)

enum class VehicleType {
    STANDARD,
    PREMIUM,
    XL
}

