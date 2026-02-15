package com.example.avaride_1.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed for Light Theme

import java.io.Serializable

data class SearchLocation(
    val name: String,
    val address: String,
    val distance: String,
    val latitude: Double,
    val longitude: Double
) : Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationSearchScreen(
    onDestinationSelected: (SearchLocation) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchLocation>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<SearchLocation?>(null) }

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    val SurfaceColor = Color(0xFFF3F4F6)

    // Mock Singapore locations (in production, use Google Places API)
    val singaporeLocations = remember {
        listOf(
            SearchLocation("Blk 174 Hougange Ave 1", "Singapore 531174", "12.5 km", 1.4010, 103.9070),
            SearchLocation("SIT Punggol Campus", "10 Dover Drive, Singapore 138683", "13.2 km", 1.4086, 103.9046),
            SearchLocation("Waterway Point", "83 Punggol Central, Singapore 828761", "13.8 km", 1.4062, 103.9022),
            SearchLocation("Changi Airport", "Airport Boulevard, Singapore 819643", "18.5 km", 1.3644, 103.9915),
            SearchLocation("Marina Bay Sands", "10 Bayfront Avenue, Singapore 018956", "14.2 km", 1.2834, 103.8607),
            SearchLocation("Jewel Changi", "78 Airport Boulevard, Singapore 819666", "18.0 km", 1.3594, 103.9890),
            SearchLocation("Sentosa Island", "Sentosa Gateway, Singapore 098269", "20.5 km", 1.2494, 103.8303),
            SearchLocation("Orchard Road", "Orchard Road, Singapore", "15.0 km", 1.3048, 103.8318),
            SearchLocation("Clarke Quay", "3 River Valley Road, Singapore 179024", "14.5 km", 1.2896, 103.8468),
            SearchLocation("Gardens by the Bay", "18 Marina Gardens Drive, Singapore 018953", "14.8 km", 1.2816, 103.8636),
            SearchLocation("Singapore Zoo", "80 Mandai Lake Road, Singapore 729826", "8.5 km", 1.4043, 103.7930),
            SearchLocation("National University Hospital", "5 Lower Kent Ridge Road, Singapore 119074", "22.0 km", 1.2935, 103.7833)
        )
    }

    // Filter locations based on search query
    LaunchedEffect(searchQuery) {
        searchResults = if (searchQuery.isBlank()) {
            singaporeLocations
        } else {
            singaporeLocations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                color = BackgroundColor,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Where to?", color = SecondaryText.copy(alpha = 0.5f))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = PrimaryText.copy(alpha = 0.7f)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = PrimaryText.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = SecondaryText.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF0A84FF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { /* Handle search */ }
                        ),
                        singleLine = true
                    )
                }
            }

            // Search Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Popular destinations",
                            color = SecondaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(searchResults) { location ->
                    LocationResultCard(
                        location = location,
                        isSelected = selectedLocation == location,
                        onCardClick = {
                            selectedLocation = if (selectedLocation == location) null else location
                        },
                        onConfirmClick = { onDestinationSelected(location) },
                        primaryText = PrimaryText,
                        secondaryText = SecondaryText,
                        surfaceColor = SurfaceColor
                    )
                }

                if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No results found",
                                color = PrimaryText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try searching for a different location",
                                color = SecondaryText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationResultCard(
    location: SearchLocation,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onConfirmClick: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    surfaceColor: Color
) {
    Surface(
        onClick = onCardClick,
        color = if (isSelected) Color(0xFF0A84FF).copy(alpha = 0.1f) else surfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isSelected) Color(0xFF0A84FF) else primaryText.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else primaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        color = primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = location.address,
                        color = secondaryText,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = location.distance,
                    color = secondaryText.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            // Show map preview when selected
            if (isSelected) {
                com.example.avaride_1.presentation.components.DestinationPreviewMap(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm button
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryText,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Select this location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

