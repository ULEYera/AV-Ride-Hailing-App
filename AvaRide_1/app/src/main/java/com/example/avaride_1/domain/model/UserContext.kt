package com.example.avaride_1.domain.model

data class UserContext(
    val currentLocation: Location,
    val currentTime: Long = System.currentTimeMillis(),
    val dayOfWeek: Int, // 1-7 (Monday-Sunday)
    val hourOfDay: Int, // 0-23
    val recentDestinations: List<Destination> = emptyList(),
    val favoriteLocations: Map<DestinationType, Destination> = emptyMap()
)

data class UserProfile(
    val name: String,
    val paymentMethod: PaymentMethod = PaymentMethod.GOOGLE_PAY,
    val preferences: UserPreferences = UserPreferences()
)

data class UserPreferences(
    val quietMode: Boolean = false,
    val preferredTemperature: Int = 22, // Celsius
    val preferredLighting: LightingMode = LightingMode.WARM,
    val musicEnabled: Boolean = true
)

enum class PaymentMethod {
    GOOGLE_PAY,
    CREDIT_CARD,
    DEBIT_CARD
}

enum class LightingMode {
    WARM,
    COOL,
    AMBIENT,
    OFF
}

