package com.example.avaride_1.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint

// Helper function to fetch route - MOVED TO OsrmRepository.kt
// Use OsrmRepository instead.

/**
 * AvaRide Map Component for displaying destination and pickup locations
 * Uses OSMDroid (OpenStreetMap)
 */
@Composable
fun AvaRideMap(
    destinationLatLng: GeoPoint,
    pickupLatLng: GeoPoint? = null,
    showRoute: Boolean = false,
    routePoints: List<GeoPoint> = emptyList(), // Added support for custom route path
    modifier: Modifier = Modifier
) {
    val markers = remember(destinationLatLng, pickupLatLng) {
        val list = mutableListOf<OsmdroidMarker>()
        list.add(OsmdroidMarker(destinationLatLng, "Destination", "Your destination"))
        pickupLatLng?.let {
            list.add(OsmdroidMarker(it, "Pickup", "Pickup location"))
        }
        list
    }

    // Use provided routePoints if available, otherwise straight line if showRoute is true
    val polylines = remember(destinationLatLng, pickupLatLng, showRoute, routePoints) {
        if (routePoints.isNotEmpty()) {
            listOf(routePoints)
        } else if (showRoute && pickupLatLng != null) {
            listOf(listOf(pickupLatLng, destinationLatLng))
        } else {
            emptyList()
        }
    }

    OsmdroidMapView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp)),
        center = pickupLatLng ?: destinationLatLng,
        zoom = 14.0,
        markers = markers,
        polylines = polylines
    )
}

/**
 * Simplified map for destination preview
 */
@Composable
fun DestinationPreviewMap(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val location = GeoPoint(latitude, longitude)

    AvaRideMap(
        destinationLatLng = location,
        pickupLatLng = null,
        showRoute = false,
        modifier = modifier
    )
}

/**
 * Full booking map showing both pickup and destination with Road Routing
 */
@Composable
fun BookingRouteMap(
    destinationLat: Double,
    destinationLng: Double,
    pickupLat: Double,
    pickupLng: Double,
    modifier: Modifier = Modifier
) {
    val destination = GeoPoint(destinationLat, destinationLng)
    val pickup = GeoPoint(pickupLat, pickupLng)
    
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    val repo = remember { com.example.avaride_1.data.repository.OsrmRepository() }

    // Fetch road-based route
    LaunchedEffect(destination, pickup) {
        routePoints = repo.fetchRoute(pickup, destination)
    }

    AvaRideMap(
        destinationLatLng = destination,
        pickupLatLng = pickup,
        showRoute = true,
        routePoints = routePoints,
        modifier = modifier
    )
}

/**
 * Live Ride Map showing moving vehicle and road-based route
 */
@Composable
fun LiveRideMap(
    vehicleLat: Double,
    vehicleLng: Double,
    destinationLat: Double,
    destinationLng: Double,
    routePoints: List<GeoPoint> = emptyList(), // Pass route from ViewModel
    modifier: Modifier = Modifier
) {
    val vehiclePos = GeoPoint(vehicleLat, vehicleLng)
    val destPos = GeoPoint(destinationLat, destinationLng)

    // Note: In LiveRideMap, we now expect the ViewModel to provide the routePoints
    // But we can keep a local fetch fallback if routePoints is empty for some reason, 
    // OR just rely on VM. For now, rely on VM as per plan.

    val markers = remember(vehiclePos, destPos) {
        listOf(
            OsmdroidMarker(vehiclePos, "Your AV", "On the way home"),
            OsmdroidMarker(destPos, "Destination")
        )
    }

    // Use fetched route if available, otherwise straight line
    val polylines = remember(vehiclePos, destPos, routePoints) {
        if (routePoints.isNotEmpty()) {
            listOf(routePoints)
        } else {
            listOf(listOf(vehiclePos, destPos))
        }
    }

    OsmdroidMapView(
        modifier = modifier.fillMaxSize(),
        center = vehiclePos, // Follow vehicle
        zoom = 16.0,
        markers = markers,
        polylines = polylines, // Road based route
        showUserLocation = true
    )
}


