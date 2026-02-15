package com.example.avaride_1.presentation.screens.inride

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.domain.model.LightingMode
// import com.example.avaride_1.presentation.components.FrostedGlassCard // Removed
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed

@Composable
fun InRideScreen(
    viewModel: InRideViewModel,
    onEmergencyStop: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val SurfaceColor = Color(0xFFFFFFFF) // White for cards
    val BackgroundSurface = Color(0xFFF3F4F6) // Light grey for controls

    // Start Journey Simulation on entering screen
    LaunchedEffect(Unit) {
        viewModel.startJourney()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live Map Background
        com.example.avaride_1.presentation.components.LiveRideMap(
            vehicleLat = uiState.currentLat,
            vehicleLng = uiState.currentLng,
            destinationLat = 1.3644, // Hardcoded Changi for demo, should match VM end
            destinationLng = 103.9915,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Banner for Trip Info (Destination & ETA)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceColor,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Heading to",
                        color = SecondaryText.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = uiState.destination,
                        color = PrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = Color(0xFF30D158).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${uiState.remainingMinutes} min",
                        color = Color(0xFF30D158),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 3. Bottom Sheet for Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = SurfaceColor,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trip Progress
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trip Progress",
                            color = SecondaryText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(uiState.progress * 100).toInt()}%",
                            color = PrimaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF0A84FF),
                        trackColor = BackgroundSurface,
                    )
                }

                EnvironmentControls(
                    temperature = uiState.temperature,
                    lightingMode = uiState.lightingMode,
                    onTemperatureChange = { viewModel.updateTemperature(it) },
                    onLightingChange = { viewModel.updateLighting(it) },
                    primaryText = PrimaryText,
                    secondaryText = SecondaryText,
                    surfaceColor = BackgroundSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                EmergencyStopButton(
                    onClick = onEmergencyStop,
                    surfaceColor = Color(0xFFFF3B30).copy(alpha = 0.1f),
                    contentColor = Color(0xFFFF3B30)
                )
            }
        }
    }
}

@Composable
private fun EnvironmentControls(
    temperature: Int,
    lightingMode: LightingMode,
    onTemperatureChange: (Int) -> Unit,
    onLightingChange: (LightingMode) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    surfaceColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cabin Comfort",
                color = primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Temperature",
                    color = secondaryText,
                    fontSize = 14.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onTemperatureChange(temperature - 1) },
                        modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
                    ) {
                        Text("−", color = primaryText, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = "${temperature}°C",
                        color = primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { onTemperatureChange(temperature + 1) },
                        modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
                    ) {
                        Text("+", color = primaryText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lighting
            Text(
                text = "Mood Lighting",
                color = secondaryText,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LightingMode.entries.forEach { mode ->
                    LightingModeChip(
                        mode = mode,
                        isSelected = mode == lightingMode,
                        onClick = { onLightingChange(mode) },
                        primaryText = primaryText,
                        secondaryText = secondaryText
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Music
             Surface(
                onClick = {}, // TODO: Music player
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎵",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Play Music",
                        color = primaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LightingModeChip(
    mode: LightingMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryText: Color,
    secondaryText: Color
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF0A84FF).copy(alpha = 0.1f)
    } else {
        Color.White
    }
    
    val borderColor = if (isSelected) Color(0xFF0A84FF) else Color.Transparent

    val icon = when (mode) {
        LightingMode.WARM -> "💡"
        LightingMode.COOL -> "❄️"
        LightingMode.AMBIENT -> "✨"
        LightingMode.OFF -> "⭕"
    }

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mode.name.take(1) + mode.name.drop(1).lowercase(),
                    color = if (isSelected) Color(0xFF0A84FF) else secondaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmergencyStopButton(
    onClick: () -> Unit,
    surfaceColor: Color,
    contentColor: Color
) {
    Surface(
        onClick = onClick,
        color = surfaceColor,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emergency Stop",
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
