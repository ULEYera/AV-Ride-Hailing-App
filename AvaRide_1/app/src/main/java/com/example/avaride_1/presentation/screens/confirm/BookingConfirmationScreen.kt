


package com.example.avaride_1.presentation.screens.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.screens.pickup.PickupPoint
import com.example.avaride_1.presentation.screens.search.SearchLocation
import kotlinx.coroutines.delay

sealed class BookingState {
    object ReviewDetails : BookingState()
    object Confirming : BookingState()
    data class AVFound(val avId: String, val etaMinutes: Int) : BookingState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    pickup: PickupPoint,
    destination: SearchLocation,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var bookingState by remember { mutableStateOf<BookingState>(BookingState.ReviewDetails) }
    var avId by remember { mutableStateOf("") }
    var etaMinutes by remember { mutableStateOf(0) }

    // Handle booking states
    LaunchedEffect(bookingState) {
        when (bookingState) {
            is BookingState.Confirming -> {
                delay(2000) // Simulate booking API call
                // Generate mock AV details
                avId = "AV-SG-${(1000..9999).random()}"
                etaMinutes = (3..6).random()
                bookingState = BookingState.AVFound(avId, etaMinutes)
            }
            is BookingState.AVFound -> {
                delay(3000) // Show AV details for 3 seconds
                onConfirm()
            }
            else -> { /* Do nothing */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient()

        when (bookingState) {
            is BookingState.ReviewDetails -> {
                ReviewDetailsContent(
                    pickup = pickup,
                    destination = destination,
                    onConfirm = { bookingState = BookingState.Confirming },
                    onBack = onBack
                )
            }
            is BookingState.Confirming -> {
                ConfirmingContent()
            }
            is BookingState.AVFound -> {
                val state = bookingState as BookingState.AVFound
                AVFoundContent(
                    avId = state.avId,
                    etaMinutes = state.etaMinutes,
                    destination = destination
                )
            }
        }
    }
}

@Composable
private fun ReviewDetailsContent(
    pickup: PickupPoint,
    destination: SearchLocation,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Surface(
            color = Color.Black.copy(alpha = 0.3f),
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
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Confirm Ride",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Route Card - BLACK BACKGROUND
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
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
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pickup.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = pickup.address,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(start = 56.dp))
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
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = destination.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = destination.address,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map showing route - BLACK BACKGROUND
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
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

                // Trip Details - BLACK BACKGROUND
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Trip Details",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow("Vehicle Type", "Standard AV", "🚗")
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Estimated Time", "18 mins", "⏱️")
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Distance", destination.distance, "📏")
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Payment", "Google Pay", "💳")

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Fare",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$12.50",
                                color = Color(0xFF30D158),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Confirm Button - BLACK BACKGROUND
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.4f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Confirm & Book Ride",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ConfirmingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF0A84FF),
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Finding your AV...",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Searching for the nearest autonomous vehicle",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AVFoundContent(
    avId: String,
    etaMinutes: Int,
    destination: SearchLocation
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon
        Surface(
            color = Color(0xFF30D158).copy(alpha = 0.2f),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "🚗", fontSize = 64.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "AV Found!",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AV Details Card - BLACK BACKGROUND
        Surface(
            color = Color.Black.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Vehicle ID",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = avId,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Arriving in",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$etaMinutes mins",
                            color = Color(0xFF30D158),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Destination",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = destination.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Standard AV",
                        color = Color(0xFF0A84FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preparing your ride experience...",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: String) {
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
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp
            )
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

