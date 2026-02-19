


package com.example.avaride_1.presentation.screens.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed for Light Theme
import com.example.avaride_1.presentation.screens.pickup.PickupPoint
import com.example.avaride_1.presentation.screens.search.SearchLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    pickup: PickupPoint,
    destination: SearchLocation,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    val SurfaceColor = Color(0xFFF3F4F6)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
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

                    Text(
                        text = "Confirm Ride",
                        color = PrimaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Route Card - LIGHT BACKGROUND
                    Surface(
                        color = SurfaceColor,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Pickup
                            Row(verticalAlignment = Alignment.Top) {
                                Surface(
                                    color = Color(0xFF0A84FF),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "📌", fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "PICKUP",
                                        color = SecondaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pickup.name,
                                        color = PrimaryText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = pickup.address,
                                        color = SecondaryText,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), modifier = Modifier.padding(start = 56.dp))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Destination
                            Row(verticalAlignment = Alignment.Top) {
                                Surface(
                                    color = Color(0xFF30D158),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "📍", fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "DESTINATION",
                                        color = SecondaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = destination.name,
                                        color = PrimaryText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = destination.address,
                                        color = SecondaryText,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Map showing route - LIGHT BACKGROUND
                    Surface(
                        color = SurfaceColor,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            com.example.avaride_1.presentation.components.BookingRouteMap(
                                destinationLat = destination.latitude,
                                destinationLng = destination.longitude,
                                pickupLat = 1.3048, // Mock pickup coordinates (Orchard)
                                pickupLng = 103.8318,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trip Details - LIGHT BACKGROUND
                    Surface(
                        color = SurfaceColor,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Trip Details",
                                color = PrimaryText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            DetailRow("Vehicle Type", "Standard AV", "🚗", PrimaryText, SecondaryText)
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow("Estimated Time", "18 mins", "⏱️", PrimaryText, SecondaryText)
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow("Distance", destination.distance, "📏", PrimaryText, SecondaryText)
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow("Payment", "Google Pay", "💳", PrimaryText, SecondaryText)

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Fare",
                                    color = PrimaryText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$12.50",
                                    color = Color(0xFF30D158), // Keeping Green for money
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText, // Black/Dark Grey button for high contrast
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Confirm & Pay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: String, primaryColor: Color, secondaryColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = secondaryColor,
                fontSize = 15.sp
            )
        }
        Text(
            text = value,
            color = primaryColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

