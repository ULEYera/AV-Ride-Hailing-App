package com.example.avaride_1.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated mesh gradient background inspired by Apple Intelligence UI
 * Creates a flowing, organic gradient that shifts over time
 * Dark purple theme for premium AV ride-hailing experience
 */
@Composable
fun GlowingMeshGradient(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF0D0221), // Deep midnight purple
        Color(0xFF1A0B2E), // Dark royal purple
        Color(0xFF240B3B), // Rich deep purple
        Color(0xFF2D1B47)  // Dark violet-purple
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_gradient")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_animation"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Create multiple gradient circles that move
        val circles = listOf(
            GradientCircle(
                center = Offset(
                    width * (0.2f + 0.3f * cos(animationProgress * 2 * PI.toFloat())),
                    height * (0.3f + 0.2f * sin(animationProgress * 2 * PI.toFloat()))
                ),
                radius = width * 0.6f,
                color = colors[0]
            ),
            GradientCircle(
                center = Offset(
                    width * (0.8f + 0.2f * cos((animationProgress + 0.3f) * 2 * PI.toFloat())),
                    height * (0.7f + 0.3f * sin((animationProgress + 0.3f) * 2 * PI.toFloat()))
                ),
                radius = width * 0.7f,
                color = colors[1]
            ),
            GradientCircle(
                center = Offset(
                    width * (0.5f + 0.25f * cos((animationProgress + 0.6f) * 2 * PI.toFloat())),
                    height * (0.5f + 0.25f * sin((animationProgress + 0.6f) * 2 * PI.toFloat()))
                ),
                radius = width * 0.5f,
                color = colors[2]
            )
        )

        // Draw overlapping radial gradients
        circles.forEach { circle ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        circle.color.copy(alpha = 0.6f),
                        circle.color.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = circle.center,
                    radius = circle.radius
                ),
                center = circle.center,
                radius = circle.radius
            )
        }
    }
}

private data class GradientCircle(
    val center: Offset,
    val radius: Float,
    val color: Color
)


