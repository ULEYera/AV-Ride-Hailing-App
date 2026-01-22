package com.example.avaride_1.presentation.screens.pickup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.screens.search.SearchLocation
import java.io.Serializable

data class PickupPoint(
    val name: String,
    val address: String,
    val eta: String, // Walking time
    val isCurrentLocation: Boolean = false
) : Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupSelectionScreen(
    destination: SearchLocation,
    onPickupSelected: (PickupPoint) -> Unit,
    onBack: () -> Unit
) {
    // Mock pickup points based on GPS (in production, use actual GPS + nearby points)
    val pickupPoints = remember {
        listOf(
            PickupPoint(
                name = "Current Location",
                address = "Orchard Road, Singapore",
                eta = "0 min",
                isCurrentLocation = true
            ),
            PickupPoint(
                name = "Orchard MRT Station",
                address = "Orchard Boulevard Exit",
                eta = "2 min walk"
            ),
            PickupPoint(
                name = "ION Orchard",
                address = "Main Entrance, Level 1",
                eta = "3 min walk"
            ),
            PickupPoint(
                name = "Wisma Atria",
                address = "Taxi Stand",
                eta = "4 min walk"
            ),
            PickupPoint(
                name = "Plaza Singapura",
                address = "Basement Pickup Point",
                eta = "5 min walk"
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
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
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Pickup Point",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Going to ${destination.name}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card with better messaging
            Surface(
                color = Color.Black.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Looks like you are near these places.\nPick your most preferred pickup point.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pickup Points List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pickupPoints) { pickup ->
                    PickupPointCard(
                        pickup = pickup,
                        onClick = { onPickupSelected(pickup) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PickupPointCard(
    pickup: PickupPoint,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (pickup.isCurrentLocation)
            Color(0xFF0A84FF).copy(alpha = 0.2f)
        else
            Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (pickup.isCurrentLocation)
                    Color(0xFF0A84FF).copy(alpha = 0.3f)
                else
                    Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (pickup.isCurrentLocation)
                            Icons.Default.Place
                        else
                            Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (pickup.isCurrentLocation)
                            Color(0xFF0A84FF)
                        else
                            Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pickup.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (pickup.isCurrentLocation) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF0A84FF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GPS",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pickup.address,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Text(
                text = pickup.eta,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

