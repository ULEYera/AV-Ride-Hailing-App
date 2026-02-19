package com.example.avaride_1.presentation.navigation

sealed class AppState {
    object Onboarding : AppState()
    object Home : AppState()
    data class Booking(val destination: com.example.avaride_1.domain.model.Destination) : AppState()
    object BookingStatus : AppState()
    data class InRide(val ride: com.example.avaride_1.domain.model.Ride) : AppState()
    object Settings : AppState()
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object DestinationSearch : Screen("destination_search")
    object PickupSelection : Screen("pickup_selection")
    object BookingConfirmation : Screen("booking_confirmation")
    object BookingStatus : Screen("booking_status")
    object Booking : Screen("booking")
    object InRide : Screen("in_ride")
    object Settings : Screen("settings")
}

