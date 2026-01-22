package com.example.avaride_1.presentation.screens.booking

import androidx.lifecycle.ViewModel
import com.example.avaride_1.presentation.screens.pickup.PickupPoint
import com.example.avaride_1.presentation.screens.search.SearchLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel to hold booking data across navigation screens
 * This solves the savedStateHandle navigation issue
 */
class BookingViewModel : ViewModel() {

    private val _destination = MutableStateFlow<SearchLocation?>(null)
    val destination: StateFlow<SearchLocation?> = _destination.asStateFlow()

    private val _pickup = MutableStateFlow<PickupPoint?>(null)
    val pickup: StateFlow<PickupPoint?> = _pickup.asStateFlow()

    fun setDestination(destination: SearchLocation) {
        println("BookingViewModel: Setting destination - ${destination.name}")
        _destination.value = destination
    }

    fun setPickup(pickup: PickupPoint) {
        println("BookingViewModel: Setting pickup - ${pickup.name}")
        _pickup.value = pickup
    }

    fun clearBookingData() {
        println("BookingViewModel: Clearing booking data")
        _destination.value = null
        _pickup.value = null
    }

    fun hasCompleteBookingData(): Boolean {
        return _destination.value != null && _pickup.value != null
    }
}

