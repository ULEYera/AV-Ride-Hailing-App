package com.example.avaride_1.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint

/**
 * AvaRide Map Component for displaying destination and pickup locations
 * Uses OSMDroid (OpenStreetMap)
 */
@Composable
fun AvaRideMap(
    destinationLatLng: GeoPoint,
    pickupLatLng: GeoPoint? = null,
    showRoute: Boolean = false,
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

    val polylines = remember(destinationLatLng, pickupLatLng, showRoute) {
        if (showRoute && pickupLatLng != null) {
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
 * Full booking map showing both pickup and destination
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

    AvaRideMap(
        destinationLatLng = destination,
        pickupLatLng = pickup,
        showRoute = true,
        modifier = modifier
    )
}

/**
 * Live Ride Map showing moving vehicle
 */
@Composable
fun LiveRideMap(
    vehicleLat: Double,
    vehicleLng: Double,
    destinationLat: Double,
    destinationLng: Double,
    modifier: Modifier = Modifier
) {
    val vehiclePos = GeoPoint(vehicleLat, vehicleLng)
    val destPos = GeoPoint(destinationLat, destinationLng)

    val markers = remember(vehiclePos, destPos) {
        listOf(
            OsmdroidMarker(vehiclePos, "Your AV", "On the way home"),
            OsmdroidMarker(destPos, "Destination")
        )
    }

    val polylines = remember(vehiclePos, destPos) {
        listOf(listOf(vehiclePos, destPos))
    }

    OsmdroidMapView(
        modifier = modifier.fillMaxSize(),
        center = vehiclePos, // Follow vehicle
        zoom = 16.0,
        markers = markers,
        polylines = polylines, // Route polyline
        showUserLocation = true // Live GPS if enabled on device
    )
}


