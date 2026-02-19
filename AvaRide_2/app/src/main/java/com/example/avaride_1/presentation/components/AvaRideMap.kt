package com.example.avaride_1.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint

// Helper function to fetch route - MOVED TO OsrmRepository.kt
// Use OsrmRepository instead.

/**
 * AvaRide Map Component for displaying destination and pickup locations
 * Uses OSMDroid (OpenStreetMap)
 */
// [PART 3] Integrate Google Maps SDK (US-05) (Currently OSMDroid)
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
 * Tracking button should be placed by the parent composable for proper positioning
 */
@Composable
fun LiveRideMap(
    vehicleLat: Double,
    vehicleLng: Double,
    destinationLat: Double,
    destinationLng: Double,
    routePoints: List<GeoPoint> = emptyList(),
    isTracking: Boolean = true,
    onUserInteraction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val vehiclePos = remember(vehicleLat, vehicleLng) {
        GeoPoint(vehicleLat, vehicleLng)
    }

    // Offset the center point so vehicle appears in the VISIBLE portion of the map
    // Since bottom ~60% of screen is covered by UI, we shift the map center DOWN
    // so the vehicle appears in the top portion of the map view
    val offsetCenterForTracking = remember(vehicleLat, vehicleLng) {
        // Shift center approximately 0.006 degrees south (~600m)
        // This makes the vehicle appear in upper third of map instead of center
        GeoPoint(vehicleLat - 0.006, vehicleLng)
    }

    // Use custom marker types for clear visibility
    val markers = remember(vehicleLat, vehicleLng, destinationLat, destinationLng) {
        listOf(
            OsmdroidMarker(
                position = GeoPoint(vehicleLat, vehicleLng),
                title = "Your AV",
                snippet = "On the way",
                type = MarkerType.VEHICLE
            ),
            OsmdroidMarker(
                position = GeoPoint(destinationLat, destinationLng),
                title = "Destination",
                snippet = "Changi Airport",
                type = MarkerType.DESTINATION
            )
        )
    }

    // Use fetched route if available, otherwise straight line
    val polylines = remember(routePoints, vehicleLat, vehicleLng, destinationLat, destinationLng) {
        if (routePoints.isNotEmpty()) {
            listOf(routePoints)
        } else {
            listOf(listOf(GeoPoint(vehicleLat, vehicleLng), GeoPoint(destinationLat, destinationLng)))
        }
    }

    // Map with tracking control - use offset center when tracking
    TrackableMapView(
        center = offsetCenterForTracking,
        vehiclePosition = vehiclePos,
        zoom = 15.0,
        markers = markers,
        polylines = polylines,
        isTracking = isTracking,
        onUserInteraction = onUserInteraction,
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Internal map view with tracking control exposed
 */
@Composable
private fun TrackableMapView(
    center: GeoPoint,
    vehiclePosition: GeoPoint,
    zoom: Double,
    markers: List<OsmdroidMarker>,
    polylines: List<List<GeoPoint>>,
    isTracking: Boolean,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    val isInitialized = remember { mutableStateOf(false) }
    val isUserTouching = remember { mutableStateOf(false) }

    val mapView = remember {
        org.osmdroid.config.Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName

        org.osmdroid.views.MapView(context).apply {
            id = android.view.View.generateViewId()
            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

            setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        isUserTouching.value = true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        isUserTouching.value = false
                        onUserInteraction()
                    }
                }
                false
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            // Initial setup
            if (!isInitialized.value) {
                map.controller.setZoom(zoom)
                map.controller.setCenter(center)
                isInitialized.value = true
            } else if (isTracking && !isUserTouching.value) {
                // Auto-follow when tracking is enabled
                map.controller.animateTo(center)
            }

            // Clear and rebuild overlays
            map.overlays.clear()

            // Add markers
            markers.forEach { m ->
                val marker = org.osmdroid.views.overlay.Marker(map)
                marker.position = m.position
                marker.title = m.title
                marker.snippet = m.snippet

                when (m.type) {
                    MarkerType.VEHICLE -> {
                        val vehicleDrawable = android.graphics.drawable.GradientDrawable().apply {
                            shape = android.graphics.drawable.GradientDrawable.OVAL
                            setSize(60, 60)
                            setColor(android.graphics.Color.parseColor("#0A84FF"))
                            setStroke(6, android.graphics.Color.WHITE)
                        }
                        marker.icon = vehicleDrawable
                        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    }
                    MarkerType.DESTINATION -> {
                        val destDrawable = android.graphics.drawable.GradientDrawable().apply {
                            shape = android.graphics.drawable.GradientDrawable.OVAL
                            setSize(48, 48)
                            setColor(android.graphics.Color.parseColor("#FF3B30"))
                            setStroke(4, android.graphics.Color.WHITE)
                        }
                        marker.icon = destDrawable
                        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    }
                    MarkerType.PICKUP -> {
                        val pickupDrawable = android.graphics.drawable.GradientDrawable().apply {
                            shape = android.graphics.drawable.GradientDrawable.OVAL
                            setSize(48, 48)
                            setColor(android.graphics.Color.parseColor("#30D158"))
                            setStroke(4, android.graphics.Color.WHITE)
                        }
                        marker.icon = pickupDrawable
                        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    }
                    MarkerType.DEFAULT -> {
                        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    }
                }
                map.overlays.add(marker)
            }

            // Add polylines
            polylines.forEach { path ->
                val poly = org.osmdroid.views.overlay.Polyline()
                poly.setPoints(path)
                poly.outlinePaint.color = android.graphics.Color.parseColor("#0A84FF")
                poly.outlinePaint.strokeWidth = 15f
                map.overlays.add(poly)
            }

            map.invalidate()
        }
    )
}


