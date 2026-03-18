package com.example.avaride_1.presentation.screens.nfc

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.FrostedButton
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * QR Code Unlock Screen - Fallback for devices without NFC
 *
 * Generates a QR code containing a short-lived unlock token.
 * The vehicle's camera scans this QR to verify the booking.
 */
@Composable
fun QRUnlockScreen(
    tripId: String,
    vehicleId: String,
    userId: String,
    sessionToken: String,
    expiresAt: Long,
    onUnlocked: () -> Unit,
    onSwitchToNFC: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    // Mutable token and expiry so the QR can be refreshed without re-navigating
    var currentToken by remember { mutableStateOf(sessionToken) }
    var currentExpiresAt by remember { mutableStateOf(expiresAt) }

    var remainingSeconds by remember { mutableStateOf(0L) }
    var isExpired by remember { mutableStateOf(false) }

    // Countdown timer — restarts whenever the token is refreshed
    LaunchedEffect(currentExpiresAt) {
        isExpired = false
        while (true) {
            val remaining = (currentExpiresAt - System.currentTimeMillis()) / 1000
            remainingSeconds = maxOf(0, remaining)
            isExpired = remaining <= 0
            if (remaining <= 0) break
            delay(1000)
        }
    }

    // QR data encodes booking context + one-time token
    val qrData = remember(tripId, currentToken) {
        buildQRData(tripId, vehicleId, userId, currentToken)
    }

    // Generate the real QR bitmap on a background thread via ZXing
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(qrData) {
        qrBitmap = null // clear stale bitmap while generating
        qrBitmap = withContext(Dispatchers.Default) {
            generateQRBitmap(qrData, 300)
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
            Text(
                text = "📷",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scan to Unlock",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Show this code to the vehicle's scanner",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // QR Code display
            if (!isExpired && qrBitmap != null) {
                QRCodeDisplay(
                    bitmap = qrBitmap,
                    vehicleId = vehicleId
                )
            } else if (isExpired) {
                ExpiredContent(onRefresh = {
                    currentToken = java.util.UUID.randomUUID().toString()
                    currentExpiresAt = System.currentTimeMillis() + 5 * 60 * 1000L
                })
            } else {
                // Loading
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Countdown
            if (!isExpired && remainingSeconds > 0) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                Text(
                    text = "Code expires in ${minutes}:${seconds.toString().padStart(2, '0')}",
                    color = if (remainingSeconds < 60) Color(0xFFFF9500) else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm button — user taps after the vehicle's camera reads the QR
            if (!isExpired && qrBitmap != null) {
                FrostedButton(
                    text = "Confirm Entry",
                    onClick = onUnlocked,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap after the vehicle scanner reads your code",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Switch to NFC button
            TextButton(onClick = onSwitchToNFC) {
                Text(
                    text = "Use NFC instead",
                    color = Color(0xFF0A84FF),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }

        // Security badge
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
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
                text = "One-time secure code",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun QRCodeDisplay(
    bitmap: Bitmap,
    vehicleId: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // QR code with animated border
        val infiniteTransition = rememberInfiniteTransition(label = "qr_pulse")
        val borderAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "border_alpha"
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = borderAlpha * 0.3f))
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code for vehicle unlock",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Vehicle: ${vehicleId.take(12)}",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ExpiredContent(onRefresh: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⏱️",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Code Expired",
            color = Color(0xFFFF9500),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please generate a new code",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        FrostedButton(
            text = "Refresh Code",
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

/**
 * Build QR data string containing unlock information
 */
private fun buildQRData(
    tripId: String,
    vehicleId: String,
    userId: String,
    sessionToken: String
): String {
    // Format: avaride://unlock?trip={tripId}&vehicle={vehicleId}&user={userId}&token={token}
    return "avaride://unlock?trip=$tripId&vehicle=$vehicleId&user=$userId&token=$sessionToken"
}

/**
 * Generate a real, scannable QR code bitmap using ZXing.
 * Encodes [data] as QR_CODE at [size] × [size] pixels.
 * Must be called from a background thread (use Dispatchers.Default).
 */
private fun generateQRBitmap(data: String, size: Int): Bitmap? {
    return try {
        val hints = mapOf(
            com.google.zxing.EncodeHintType.MARGIN to 1,
            com.google.zxing.EncodeHintType.ERROR_CORRECTION to
                com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
        )
        val bitMatrix = com.google.zxing.MultiFormatWriter().encode(
            data,
            com.google.zxing.BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

