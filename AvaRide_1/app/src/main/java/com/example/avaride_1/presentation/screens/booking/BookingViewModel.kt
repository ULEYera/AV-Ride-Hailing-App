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

    private val _isRideActive = MutableStateFlow(false)
    val isRideActive: StateFlow<Boolean> = _isRideActive.asStateFlow()

    private val _arrivalTime = MutableStateFlow<Long?>(null)
    val arrivalTime: StateFlow<Long?> = _arrivalTime.asStateFlow()

    fun setDestination(destination: SearchLocation) {
        println("BookingViewModel: Setting destination - ${destination.name}")
        _destination.value = destination
    }

    fun setPickup(pickup: PickupPoint) {
        println("BookingViewModel: Setting pickup - ${pickup.name}")
        _pickup.value = pickup
    }

    fun setRideActive(isActive: Boolean) {
        _isRideActive.value = isActive
        if (isActive && _arrivalTime.value == null) {
            // Start timer if not already set (20 seconds default)
            _arrivalTime.value = System.currentTimeMillis() + 20 * 1000
        } else if (!isActive) {
            _arrivalTime.value = null
        }
    }

    fun clearBookingData() {
        println("BookingViewModel: Clearing booking data")
        _destination.value = null
        _pickup.value = null
        _isRideActive.value = false
        _arrivalTime.value = null
    }

    fun hasCompleteBookingData(): Boolean {
        return _destination.value != null && _pickup.value != null
    }

    fun getRemainingSeconds(): Long {
        val target = _arrivalTime.value ?: return 0
        val remaining = (target - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0
    }
}
