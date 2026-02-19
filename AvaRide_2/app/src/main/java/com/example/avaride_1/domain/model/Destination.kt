package com.example.avaride_1.domain.model

data class Destination(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val confidence: Float = 0.0f, // AI prediction confidence (0.0 - 1.0)
    val type: DestinationType = DestinationType.CUSTOM
)

enum class DestinationType {
    HOME,
    WORK,
    FAVORITE,
    CUSTOM
}
