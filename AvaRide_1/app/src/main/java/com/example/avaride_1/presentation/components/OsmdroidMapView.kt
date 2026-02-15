package com.example.avaride_1.presentation.components

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

data class OsmdroidMarker(
    val position: GeoPoint,
    val title: String = "",
    val snippet: String = ""
)

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double = 15.0,
    markers: List<OsmdroidMarker> = emptyList(),
    polylines: List<List<GeoPoint>> = emptyList(),
    showUserLocation: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        // Load configuration (User Agent)
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        
        MapView(context).apply {
            id = View.generateViewId()
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
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
            map.controller.setZoom(zoom)
            map.controller.animateTo(center)
            
            // Clear existing overlays but preserve LocationOverlay if needed
            // To prevent flickering, we could update existing markers, but for simplicity we recreate non-permanent ones
            // We'll just clear all and re-add for now as the list is small
            map.overlays.clear()

            // User Location
            if (showUserLocation) {
                val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                locOverlay.enableMyLocation()
                map.overlays.add(locOverlay)
            }
            
            // Markers
            markers.forEach { m ->
                val marker = Marker(map)
                marker.position = m.position
                marker.title = m.title
                marker.snippet = m.snippet
                // Default icon is used
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(marker)
            }
            
            // Polylines
            polylines.forEach { path ->
                val poly = Polyline()
                poly.setPoints(path)
                poly.outlinePaint.color = android.graphics.Color.parseColor("#0A84FF") // Theme Blue
                poly.outlinePaint.strokeWidth = 15f 
                map.overlays.add(poly)
            }
            
            map.invalidate()
        }
    )
}
