package com.example.avaride_1.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Pulsating orb animation inspired by Apple Siri
 * Used for loading states and conversational UI
 */
@Composable
fun PulsatingOrb(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF0A84FF) // iOS blue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_alpha"
    )

    Canvas(modifier = modifier.size(120.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 * scale

        // Outer glow
        drawCircle(
            color = color.copy(alpha = alpha * 0.3f),
            radius = radius * 1.3f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )

        // Middle ring
        drawCircle(
            color = color.copy(alpha = alpha * 0.6f),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Core
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius * 0.5f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

