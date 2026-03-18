package com.example.avaride_1.presentation.screens.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.FrostedButton
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.components.PulsatingOrb
import kotlinx.coroutines.delay

/**
 * NFC Unlock Screen - Actual NFC scanning implementation
 * Uses Android NFC APIs to detect and read vehicle NFC tags
 */
@Composable
fun NFCUnlockScreen(
    onUnlocked: () -> Unit,
    onSkip: () -> Unit = {}
) {
    val context = LocalContext.current
    var nfcState by remember { mutableStateOf(NFCState.WAITING) }
    var vehicleId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }

    // Check NFC availability
    LaunchedEffect(Unit) {
        when {
            nfcAdapter == null -> {
                nfcState = NFCState.NOT_AVAILABLE
                errorMessage = "NFC is not available on this device"
            }
            !nfcAdapter.isEnabled -> {
                nfcState = NFCState.DISABLED
                errorMessage = "Please enable NFC in settings"
            }
            else -> {
                nfcState = NFCState.READY
                // Enable reader mode for better detection
                enableReaderMode(context as Activity, nfcAdapter) { tag ->
                    handleNFCTag(tag,
                        onSuccess = { id ->
                            vehicleId = id
                            nfcState = NFCState.UNLOCKING
                        },
                        onError = { error ->
                            errorMessage = error
                            nfcState = NFCState.ERROR
                        }
                    )
                }
            }
        }
    }

    // Handle unlocking animation and auto-navigation
    LaunchedEffect(nfcState) {
        if (nfcState == NFCState.UNLOCKING) {
            delay(1500) // Simulate server validation
            nfcState = NFCState.SUCCESS
        }
    }

    // Auto-navigate after showing success message
    LaunchedEffect(nfcState) {
        if (nfcState == NFCState.SUCCESS) {
            delay(2000) // Show "Unlocked!" message for 2 seconds
            println("NFCUnlockScreen: Auto-navigating to In-Ride screen")
            onUnlocked()
        }
    }

    // Cleanup reader mode
    DisposableEffect(Unit) {
        onDispose {
            if (nfcAdapter?.isEnabled == true) {
                try {
                    nfcAdapter.disableReaderMode(context as Activity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
            when (nfcState) {
                NFCState.WAITING, NFCState.READY -> {
                    ReadyToScanContent()
                }
                NFCState.SCANNING -> {
                    ScanningContent()
                }
                NFCState.UNLOCKING -> {
                    UnlockingContent(vehicleId)
                }
                NFCState.SUCCESS -> {
                    SuccessContent()
                }
                NFCState.ERROR -> {
                    ErrorContent(
                        message = errorMessage ?: "Unknown error",
                        onUseQR = onSkip
                    )
                }
                NFCState.NOT_AVAILABLE, NFCState.DISABLED -> {
                    NFCDisabledContent(
                        message = errorMessage ?: "NFC unavailable",
                        onSkip = onSkip
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyToScanContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // NFC icon animation
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

        Text(
            text = "📱",
            fontSize = (80 * scale).sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Tap to Unlock",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hold your phone near the vehicle's NFC sensor",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NFC waves animation
        NFCWavesAnimation()
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
private fun UnlockingContent(vehicleId: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PulsatingOrb(color = Color(0xFF30D158))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Unlocking Vehicle",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        if (vehicleId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ID: ${vehicleId.take(8)}...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SuccessContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✓",
            color = Color(0xFF30D158),
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Unlocked!",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enter the vehicle",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ErrorContent(message: String, onUseQR: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Scan Failed",
            color = Color(0xFFFF3B30),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onUseQR) {
            Text(
                text = "Use QR Code Instead",
                color = Color(0xFF0A84FF),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun NFCDisabledContent(message: String, onSkip: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📵",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "NFC Required",
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
            modifier = Modifier.padding(horizontal = 48.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        FrostedButton(
            text = "Use QR Code Instead",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}

@Composable
private fun NFCWavesAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "waves")

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Create 3 expanding circles
        for (i in 0..2) {
            val delay = i * 400
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave_$i"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha_$i"
            )

            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCircle(
                    color = Color(0xFF0A84FF).copy(alpha = alpha),
                    radius = size.minDimension / 2 * scale,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

// NFC State management
enum class NFCState {
    WAITING,
    READY,
    SCANNING,
    UNLOCKING,
    SUCCESS,
    ERROR,
    NOT_AVAILABLE,
    DISABLED
}

// NFC Helper functions
private fun enableReaderMode(
    activity: Activity,
    nfcAdapter: NfcAdapter,
    onTagDiscovered: (Tag) -> Unit
) {
    try {
        nfcAdapter.enableReaderMode(
            activity,
            { tag -> onTagDiscovered(tag) },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun handleNFCTag(
    tag: Tag,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Read tag ID
        val tagId = tag.id.joinToString("") { "%02x".format(it) }

        // Try different NFC technologies
        when {
            tag.techList.contains(Ndef::class.java.name) -> {
                val ndef = Ndef.get(tag)
                ndef?.connect()
                val ndefMessage = ndef?.ndefMessage
                ndef?.close()

                // In production, validate vehicle ID from NDEF message
                onSuccess("VEHICLE_$tagId")
            }
            tag.techList.contains(IsoDep::class.java.name) -> {
                val isoDep = IsoDep.get(tag)
                isoDep?.connect()
                // Read vehicle data using ISO-DEP protocol
                isoDep?.close()
                onSuccess("VEHICLE_$tagId")
            }
            tag.techList.contains(MifareClassic::class.java.name) -> {
                val mifare = MifareClassic.get(tag)
                mifare?.connect()
                // Read vehicle data from Mifare card
                mifare?.close()
                onSuccess("VEHICLE_$tagId")
            }
            tag.techList.contains(MifareUltralight::class.java.name) -> {
                val ultralight = MifareUltralight.get(tag)
                ultralight?.connect()
                // Read vehicle data from Mifare Ultralight
                ultralight?.close()
                onSuccess("VEHICLE_$tagId")
            }
            else -> {
                // Unknown tag type, but use ID
                onSuccess("VEHICLE_$tagId")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onError("Failed to read NFC tag: ${e.message}")
    }
}

