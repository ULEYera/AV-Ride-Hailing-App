package com.example.avaride_1.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Frosted glass card component mimicking iOS UltraThinMaterial
 * Uses blur and semi-transparent backgrounds for glassmorphism effect
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // Background blur layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Color.Black.copy(alpha = 0.3f)
                )
                .blur(20.dp)
        )

        // Content layer with border
        Column(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Color.Black.copy(alpha = 0.25f)
                )
                .padding(24.dp),
            content = content
        )
    }
}

/**
 * Minimal frosted button with glassmorphism
 */
@Composable
fun FrostedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.4f),
            contentColor = Color.White,
            disabledContainerColor = Color.Black.copy(alpha = 0.2f),
            disabledContentColor = Color.White.copy(alpha = 0.3f)
        )
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

