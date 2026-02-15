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
import kotlinx.coroutines.delay

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
    var remainingSeconds by remember { mutableStateOf(0L) }
    var isExpired by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(expiresAt) {
        while (true) {
            val remaining = (expiresAt - System.currentTimeMillis()) / 1000
            remainingSeconds = maxOf(0, remaining)
            isExpired = remaining <= 0
            if (remaining <= 0) break
            delay(1000)
        }
    }

    // Generate QR code data
    val qrData = remember(tripId, sessionToken) {
        buildQRData(tripId, vehicleId, userId, sessionToken)
    }

    // Generate QR bitmap
    val qrBitmap = remember(qrData) {
        generateQRBitmap(qrData, 300)
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
                ExpiredContent(onRefresh = { /* Regenerate token */ })
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

            Spacer(modifier = Modifier.height(32.dp))

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
 * Generate QR code bitmap
 * Note: In production, use ZXing or ML Kit for QR generation
 */
private fun generateQRBitmap(data: String, size: Int): Bitmap? {
    return try {
        // Placeholder - in production use com.google.zxing:core
        // For now, create a simple placeholder bitmap
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        // Draw a simple pattern (would be replaced with actual QR generation)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.FILL
        }

        // Draw placeholder pattern
        val blockSize = size / 21f

        // Position detection patterns (corners)
        drawPositionPattern(canvas, paint, 0f, 0f, blockSize)
        drawPositionPattern(canvas, paint, (size - 7 * blockSize), 0f, blockSize)
        drawPositionPattern(canvas, paint, 0f, (size - 7 * blockSize), blockSize)

        // Draw some data modules (simplified)
        val hash = data.hashCode()
        for (i in 8 until 13) {
            for (j in 8 until 13) {
                if ((hash + i + j) % 2 == 0) {
                    canvas.drawRect(
                        i * blockSize, j * blockSize,
                        (i + 1) * blockSize, (j + 1) * blockSize,
                        paint
                    )
                }
            }
        }

        bitmap
    } catch (e: Exception) {
        null
    }
}

private fun drawPositionPattern(
    canvas: android.graphics.Canvas,
    paint: android.graphics.Paint,
    x: Float,
    y: Float,
    blockSize: Float
) {
    // Outer square
    canvas.drawRect(x, y, x + 7 * blockSize, y + 7 * blockSize, paint)

    // Inner white square
    paint.color = android.graphics.Color.WHITE
    canvas.drawRect(
        x + blockSize, y + blockSize,
        x + 6 * blockSize, y + 6 * blockSize,
        paint
    )

    // Center square
    paint.color = android.graphics.Color.BLACK
    canvas.drawRect(
        x + 2 * blockSize, y + 2 * blockSize,
        x + 5 * blockSize, y + 5 * blockSize,
        paint
    )
}

