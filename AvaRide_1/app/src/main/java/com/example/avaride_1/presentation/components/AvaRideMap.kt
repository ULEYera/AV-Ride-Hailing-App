package com.example.avaride_1.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * AvaRide Map Component for displaying destination and pickup locations
 * Uses Google Maps SDK for Compose
 */
@Composable
fun AvaRideMap(
    destinationLatLng: LatLng,
    pickupLatLng: LatLng? = null,
    showRoute: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destinationLatLng, 14f)
    }

    // Update camera when destination changes
    LaunchedEffect(destinationLatLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(destinationLatLng, 14f)
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp)),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapStyleOptions = null
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            compassEnabled = false
        )
    ) {
        // Destination marker (red)
        Marker(
            state = MarkerState(position = destinationLatLng),
            title = "Destination",
            snippet = "Your destination",
            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory
                .defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)
        )

        // Pickup marker (blue) if provided
        pickupLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Pickup",
                snippet = "Pickup location",
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory
                    .defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE)
            )
        }

        // Draw route line if both locations provided
        if (showRoute && pickupLatLng != null) {
            Polyline(
                points = listOf(pickupLatLng, destinationLatLng),
                color = androidx.compose.ui.graphics.Color(0xFF0A84FF),
                width = 10f
            )
        }
    }
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
    val location = LatLng(latitude, longitude)

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
    val destination = LatLng(destinationLat, destinationLng)
    val pickup = LatLng(pickupLat, pickupLng)

    AvaRideMap(
        destinationLatLng = destination,
        pickupLatLng = pickup,
        showRoute = true,
        modifier = modifier
    )
}

