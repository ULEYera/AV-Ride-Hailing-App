package com.example.avaride_1.presentation.components

import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

enum class MarkerType {
    VEHICLE,      // Blue pulsing dot for the AV
    DESTINATION,  // Red pin for destination
    PICKUP,       // Green pin for pickup
    DEFAULT       // Standard marker
}

data class OsmdroidMarker(
    val position: GeoPoint,
    val title: String = "",
    val snippet: String = "",
    val type: MarkerType = MarkerType.DEFAULT
)

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double = 15.0,
    markers: List<OsmdroidMarker> = emptyList(),
    polylines: List<List<GeoPoint>> = emptyList(),
    showUserLocation: Boolean = false,
    followCenter: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Track if map has been initialized - only set zoom on first render
    val isInitialized = remember { mutableStateOf(false) }

    // Track if user has manually interacted with the map (panned/zoomed)
    // Once user interacts, we stop auto-centering until they explicitly want to recenter
    val userHasInteracted = remember { mutableStateOf(false) }
    val isUserTouching = remember { mutableStateOf(false) }

    val mapView = remember {
        // Load configuration (User Agent)
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        
        MapView(context).apply {
            id = View.generateViewId()
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

            // Detect user touch to know when they're interacting
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isUserTouching.value = true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isUserTouching.value = false
                        // Mark that user has interacted (panned/zoomed manually)
                        userHasInteracted.value = true
                    }
                }
                // Return false to allow normal map interaction
                false
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            // Only set zoom/center on initial setup
            if (!isInitialized.value) {
                map.controller.setZoom(zoom)
                map.controller.setCenter(center)
                isInitialized.value = true
            } else if (followCenter && !userHasInteracted.value && !isUserTouching.value) {
                // Only auto-center if:
                // 1. followCenter is enabled
                // 2. User hasn't manually panned/zoomed
                // 3. User isn't currently touching the map
                map.controller.animateTo(center)
            }

            // Clear existing overlays
            map.overlays.clear()

            // User Location
            if (showUserLocation) {
                val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                locOverlay.enableMyLocation()
                map.overlays.add(locOverlay)
            }
            
            // Markers with custom icons based on type
            markers.forEach { m ->
                val marker = Marker(map)
                marker.position = m.position
                marker.title = m.title
                marker.snippet = m.snippet

                // Create custom marker icon based on type
                when (m.type) {
                    MarkerType.VEHICLE -> {
                        // Create a prominent blue circle for the vehicle
                        val vehicleDrawable = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setSize(60, 60)
                            setColor(android.graphics.Color.parseColor("#0A84FF"))
                            setStroke(6, android.graphics.Color.WHITE)
                        }
                        marker.icon = vehicleDrawable
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    MarkerType.DESTINATION -> {
                        // Create a red destination marker
                        val destDrawable = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setSize(48, 48)
                            setColor(android.graphics.Color.parseColor("#FF3B30"))
                            setStroke(4, android.graphics.Color.WHITE)
                        }
                        marker.icon = destDrawable
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    MarkerType.PICKUP -> {
                        // Create a green pickup marker
                        val pickupDrawable = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setSize(48, 48)
                            setColor(android.graphics.Color.parseColor("#30D158"))
                            setStroke(4, android.graphics.Color.WHITE)
                        }
                        marker.icon = pickupDrawable
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    MarkerType.DEFAULT -> {
                        // Use default marker
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                }

                map.overlays.add(marker)
            }
            
            // Polylines
            polylines.forEach { path ->
                val poly = Polyline()
                poly.setPoints(path)
                poly.outlinePaint.color = android.graphics.Color.parseColor("#0A84FF")
                poly.outlinePaint.strokeWidth = 15f
                map.overlays.add(poly)
            }
            
            map.invalidate()
        }
    )
}
