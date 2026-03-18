package com.example.avaride_1.presentation.screens.nfc

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.avaride_1.presentation.components.FrostedButton
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.components.PulsatingOrb
import com.example.avaride_1.presentation.viewmodel.NFCUnlockUiState
import com.example.avaride_1.presentation.viewmodel.NFCUnlockViewModel
import kotlinx.coroutines.delay

/**
 * Secure NFC Unlock Screen with ViewModel integration.
 *
 * Features:
 * - Secure challenge-response handshake
 * - Session token management
 * - HCE service coordination
 * - Expiry countdown
 * - QR fallback option
 */
@Composable
fun SecureNFCUnlockScreen(
    tripId: String,
    vehicleId: String,
    userId: String,
    vehicleName: String = "AvaRide Vehicle",
    vehiclePlate: String = "",
    onUnlocked: () -> Unit,
    onSkip: () -> Unit = {},
    onSwitchToQR: () -> Unit = {},
    viewModel: NFCUnlockViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    val vehicleInfo by viewModel.vehicleInfo.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()

    // Initialize session when screen loads
    LaunchedEffect(tripId, vehicleId) {
        viewModel.initializeUnlock(
            tripId = tripId,
            vehicleId = vehicleId,
            userId = userId,
            vehicleName = vehicleName,
            vehiclePlate = vehiclePlate
        )
    }

    // Enable reader mode when ready
    LaunchedEffect(uiState) {
        if (uiState is NFCUnlockUiState.ReadyToScan && activity != null) {
            viewModel.startReaderMode(activity)
        }
    }

    // Handle success - navigate after showing success UI
    LaunchedEffect(uiState) {
        if (uiState is NFCUnlockUiState.Success) {
            delay(2000) // Show success for 2 seconds
            onUnlocked()
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { viewModel.cleanup(it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Vehicle info header
            vehicleInfo?.let { info ->
                VehicleInfoHeader(
                    name = info.name,
                    plate = info.licensePlate
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Main content based on state
            when (val state = uiState) {
                is NFCUnlockUiState.Initial -> {
                    LoadingContent()
                }
                is NFCUnlockUiState.ReadyToScan -> {
                    ReadyToScanContent(
                        expiresAt = state.expiresAt
                    )
                }
                is NFCUnlockUiState.Scanning -> {
                    ScanningContent()
                }
                is NFCUnlockUiState.Processing -> {
                    ProcessingContent()
                }
                is NFCUnlockUiState.Success -> {
                    SuccessContent(vehicleId = state.vehicleId)
                }
                is NFCUnlockUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.retryUnlock() },
                        onSwitchToQR = onSwitchToQR
                    )
                }
                is NFCUnlockUiState.NfcUnavailable -> {
                    NFCUnavailableContent(
                        message = state.message,
                        onSwitchToQR = onSwitchToQR,
                        onSkip = onSkip
                    )
                }
                is NFCUnlockUiState.NfcDisabled -> {
                    NFCDisabledContent(
                        message = state.message,
                        onOpenSettings = {
                            context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                        },
                        onSwitchToQR = onSwitchToQR,
                        onSkip = onSkip
                    )
                }
            }
        }

        // Security badge
        SecurityBadge(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun VehicleInfoHeader(name: String, plate: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🚗",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (plate.isNotEmpty()) {
            Text(
                text = plate,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Preparing secure unlock...",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ReadyToScanContent(expiresAt: Long) {
    var remainingSeconds by remember { mutableStateOf(0L) }

    // Countdown timer
    LaunchedEffect(expiresAt) {
        while (true) {
            val remaining = (expiresAt - System.currentTimeMillis()) / 1000
            remainingSeconds = maxOf(0, remaining)
            if (remaining <= 0) break
            delay(1000)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Animated phone icon
        val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "nfc_scale"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // NFC waves
            SecureNFCWavesAnimation()

            // Phone icon
            Text(
                text = "📱",
                fontSize = (64 * scale).sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tap to Unlock",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hold your phone near the vehicle's NFC sensor",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Session expiry countdown
        if (remainingSeconds > 0) {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            Text(
                text = "Session expires in ${minutes}:${seconds.toString().padStart(2, '0')}",
                color = if (remainingSeconds < 60) Color(0xFFFF9500) else Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ScanningContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PulsatingOrb(color = Color(0xFF0A84FF))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Reading tag...",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PulsatingOrb(color = Color(0xFFFFCC00))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Verifying...",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Validating secure handshake",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SuccessContent(vehicleId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated checkmark
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF30D158).copy(alpha = 0.2f))
        ) {
            Text(
                text = "✓",
                color = Color(0xFF30D158),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Unlocked!",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Vehicle ID: ${vehicleId.take(12)}...",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please enter the vehicle",
            color = Color(0xFF30D158),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onSwitchToQR: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Unlock Failed",
            color = Color(0xFFFF3B30),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        FrostedButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSwitchToQR) {
            Text(
                text = "Use QR Code Instead",
                color = Color(0xFF0A84FF),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun NFCUnavailableContent(
    message: String,
    onSwitchToQR: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📵",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NFC Not Available",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        FrostedButton(
            text = "Use QR Code Instead",
            onClick = onSwitchToQR,
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip for Demo",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun NFCDisabledContent(
    message: String,
    onOpenSettings: () -> Unit,
    onSwitchToQR: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📵",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NFC Disabled",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        FrostedButton(
            text = "Open NFC Settings",
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSwitchToQR) {
            Text(
                text = "Use QR Code Instead",
                color = Color(0xFF0A84FF),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip for Demo",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SecurityBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔒",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Secure Handshake Protocol",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SecureNFCWavesAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "secure_waves")

    for (i in 0..2) {
        val delay = i * 400

        val scale by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing, delayMillis = delay),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave_scale_$i"
        )

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing, delayMillis = delay),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave_alpha_$i"
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = Color(0xFF0A84FF).copy(alpha = alpha),
                radius = size.minDimension / 2 * scale,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}


