package com.example.avaride_1.presentation.screens.summary

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.FrostedGlassCard
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun RideSummaryScreen(
    totalCost: Double,
    destination: String,
    onRatingSubmitted: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var hasRated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        delay(5000)
        if (hasRated) {
            onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF0F3460),
                Color(0xFF16213E),
                Color(0xFF533483)
            )
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(500)) +
                   scaleOut(targetScale = 0.9f, animationSpec = tween(500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = Color(0xFF30D158).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                color = Color(0xFF30D158),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "You've Arrived",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = destination,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", totalCost)}",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Paid with Google Pay",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (!hasRated) {
                        Text(
                            text = "How was your ride?",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RatingButton(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Good",
                                        tint = Color(0xFF30D158),
                                        modifier = Modifier.size(32.dp)
                                    )
                                },
                                onClick = {
                                    hasRated = true
                                    onRatingSubmitted(true)
                                }
                            )

                            RatingButton(
                                icon = {
                                    Text(
                                        text = "👎",
                                        fontSize = 32.sp
                                    )
                                },
                                onClick = {
                                    hasRated = true
                                    onRatingSubmitted(false)
                                }
                            )
                        }
                    } else {
                        Text(
                            text = "Thank you for your feedback",
                            color = Color(0xFF30D158).copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.12f),
        shape = CircleShape,
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}
