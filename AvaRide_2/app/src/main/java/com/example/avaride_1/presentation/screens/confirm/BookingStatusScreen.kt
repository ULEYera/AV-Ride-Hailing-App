package com.example.avaride_1.presentation.screens.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed for Light Theme
import com.example.avaride_1.presentation.screens.search.SearchLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Composable
fun BookingStatusScreen(
    destination: SearchLocation,
    arrivalTime: Long?,
    onUnlock: () -> Unit,
    onBack: () -> Unit
) {
    // If arrivalTime is set, we skip the finding phase
    var isFinding by remember { mutableStateOf(arrivalTime == null) }
    var avId by remember { mutableStateOf("AV-SG-4042") } // Mock ID
    
    // Handle Back Press
    androidx.activity.compose.BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        if (isFinding) {
            // Simulate "Finding AV" only if not already found
            delay(2000) 
            isFinding = false
        }
    }

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    val SurfaceColor = Color(0xFFF3F4F6)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        
        if (isFinding) {
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
                    color = PrimaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Authorized payment. Searching for nearest vehicle...",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Calculate remaining time
            var timeRemainingSeconds by remember { mutableStateOf(0L) }
            
            LaunchedEffect(arrivalTime) {
                if (arrivalTime != null) {
                    while (true) {
                        val remaining = (arrivalTime - System.currentTimeMillis()) / 1000
                        timeRemainingSeconds = if (remaining > 0) remaining else 0
                        if (timeRemainingSeconds <= 0) break
                        delay(1000)
                    }
                } else {
                    // Fallback simulation
                    timeRemainingSeconds = 120
                }
            }

            AVFoundContent(
                avId = avId,
                timeRemainingSeconds = timeRemainingSeconds,
                destination = destination,
                onUnlock = onUnlock,
                primaryText = PrimaryText,
                secondaryText = SecondaryText,
                surfaceColor = SurfaceColor
            )
        }
    }
}

@Composable
private fun AVFoundContent(
    avId: String,
    timeRemainingSeconds: Long,
    destination: SearchLocation,
    onUnlock: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    surfaceColor: Color
) {
    val isArrived = timeRemainingSeconds <= 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
            .imePadding(), // Adjust for keyboard
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP: AV Status ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isArrived) "AV Arrived!" else "AV on the way",
                color = primaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            val minutes = timeRemainingSeconds / 60
            val seconds = timeRemainingSeconds % 60
            val timeString = String.format("%d:%02d", minutes, seconds)
            
            Text(
                text = if (isArrived) "Ready to board" else "Arriving in $timeString",
                color = if (isArrived) Color(0xFF30D158) else Color(0xFF0A84FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // AV Details Card
            Surface(
                color = surfaceColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isArrived) Color(0xFF30D158).copy(alpha = 0.2f) else Color(0xFF0A84FF).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🚗", fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = avId,
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard AV • 4 Seats",
                            color = secondaryText,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$12.50",
                        color = primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        // --- BOTTOM: Proceed Button ---
        Button(
            onClick = onUnlock,
            enabled = isArrived, // Only enabled when arrived
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF30D158), // Green for go
                contentColor = Color.White,
                disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
                disabledContentColor = Color.Gray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isArrived) {
                Text(
                    text = "Start Ride (Simulated)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Arriving in ${timeRemainingSeconds}s...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
