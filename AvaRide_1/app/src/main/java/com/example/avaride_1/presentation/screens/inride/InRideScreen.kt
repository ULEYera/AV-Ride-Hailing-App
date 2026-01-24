package com.example.avaride_1.presentation.screens.inride

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.domain.model.LightingMode
import com.example.avaride_1.presentation.components.FrostedGlassCard
import com.example.avaride_1.presentation.components.GlowingMeshGradient

@Composable
fun InRideScreen(
    viewModel: InRideViewModel,
    onEmergencyStop: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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

        // 2. Gradients for visibility
        // Top gradient
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha=0.8f), Color.Transparent)
                    )
                )
        )
        // Bottom gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.9f))
                    )
                )
        )

        // 3. UI Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Trip Info
            Text(
                text = uiState.destination,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.remainingMinutes} min to destination",
                color = Color(0xFF30D158),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Progress Bar (replacing huge ring, making it subtle)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                 Text(
                    text = "${(uiState.progress * 100).toInt()}% Trip Complete",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF0A84FF),
                    trackColor = Color.White.copy(alpha=0.2f),
                )
            }

            EnvironmentControls(
                temperature = uiState.temperature,
                lightingMode = uiState.lightingMode,
                onTemperatureChange = { viewModel.updateTemperature(it) },
                onLightingChange = { viewModel.updateLighting(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            EmergencyStopButton(onClick = onEmergencyStop)
        }
    }
}

@Composable
private fun JourneyProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF0A84FF),
                        Color(0xFF5E5CE6),
                        Color(0xFF0A84FF)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Complete",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EnvironmentControls(
    temperature: Int,
    lightingMode: LightingMode,
    onTemperatureChange: (Int) -> Unit,
    onLightingChange: (LightingMode) -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Temperature",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { onTemperatureChange(temperature - 1) },
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "−",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${temperature}°C",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                onClick = { onTemperatureChange(temperature + 1) },
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Mood Lighting",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
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
                    onClick = { onLightingChange(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎵",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Play Music",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LightingModeChip(
    mode: LightingMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }

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
        modifier = Modifier.size(64.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 28.sp,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mode.name.take(1),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmergencyStopButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFFFF3B30).copy(alpha = 0.2f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency",
                tint = Color(0xFFFF3B30),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emergency Stop",
                color = Color(0xFFFF3B30),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

