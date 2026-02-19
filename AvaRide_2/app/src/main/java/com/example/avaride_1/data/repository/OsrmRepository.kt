package com.example.avaride_1.data.repository

import org.osmdroid.util.GeoPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// OSRM Data Models
data class OsrmResponse(val routes: List<OsrmRoute>)
data class OsrmRoute(val geometry: OsrmGeometry)
data class OsrmGeometry(val coordinates: List<List<Double>>) // [lon, lat]

class OsrmRepository {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchRoute(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        return withContext(Dispatchers.IO) {
            var result: List<GeoPoint> = emptyList()
            try {
                // OSRM Public API
                val url = "https://router.project-osrm.org/route/v1/driving/" +
                        "${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                        "?overview=full&geometries=geojson"
                
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val json = response.body?.string()
                
                if (json != null) {
                    val osrmResponse = gson.fromJson(json, OsrmResponse::class.java)
                    
                    if (osrmResponse.routes.isNotEmpty()) {
                        val coordinates = osrmResponse.routes[0].geometry.coordinates
                        result = coordinates.map { coord ->
                            // GeoJSON is [lon, lat]
                            GeoPoint(coord[1], coord[0])
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            result
        }
    }
}
