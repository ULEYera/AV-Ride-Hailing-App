package com.example.avaride_1.presentation.navigation

sealed class AppState {
    object Onboarding : AppState()
    object Home : AppState()
    data class Booking(val destination: com.example.avaride_1.domain.model.Destination) : AppState()
    object Payment : AppState() // New
    object BookingStatus : AppState() // New
    object ARWayfinding : AppState()
    object NFCUnlock : AppState()
    data class InRide(val ride: com.example.avaride_1.domain.model.Ride) : AppState()
    object RideSummary : AppState()
    object Settings : AppState()
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object DestinationSearch : Screen("destination_search")
    object PickupSelection : Screen("pickup_selection")
    object BookingConfirmation : Screen("booking_confirmation")
    object Payment : Screen("payment") // New
    object BookingStatus : Screen("booking_status") // New
    object Booking : Screen("booking")
    object ARWayfinding : Screen("ar_wayfinding")
    object NFCUnlock : Screen("nfc_unlock")
    object SecureNFCUnlock : Screen("secure_nfc_unlock/{tripId}/{vehicleId}/{userId}") {
        fun createRoute(tripId: String, vehicleId: String, userId: String): String {
            return "secure_nfc_unlock/$tripId/$vehicleId/$userId"
        }
    }
    object QRUnlock : Screen("qr_unlock/{tripId}/{vehicleId}/{userId}") {
        fun createRoute(tripId: String, vehicleId: String, userId: String): String {
            return "qr_unlock/$tripId/$vehicleId/$userId"
        }
    }
    object InRide : Screen("in_ride")
    object RideSummary : Screen("ride_summary")
    object Settings : Screen("settings")
}

