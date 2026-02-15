package com.example.avaride_1.presentation.screens.summary

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed for Light Theme
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

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    val SurfaceColor = Color(0xFFF3F4F6)

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        delay(5000)
        if (hasRated) {
            onDismiss()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        
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
                // Card now uses Light Theme styling logic implicitly if we remove FrostedGlass and use Surface
                // But FrostedGlassCard might be hardcoded to be dark/glassy. 
                // Let's check FrostedGlassCard separately or just replace it with a standard Surface for this Light Theme screen.
                // Replacing with Surface for consistency with other Light Theme screens.
                Surface(
                    color = Color.White,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFF30D158).copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(80.dp)
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
                            color = PrimaryText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = destination,
                            color = SecondaryText,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Surface(
                            color = SurfaceColor,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Total",
                                    color = SecondaryText,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", totalCost)}",
                                    color = PrimaryText,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Paid with Google Pay",
                                    color = SecondaryText.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (!hasRated) {
                            Text(
                                text = "How was your ride?",
                                color = SecondaryText,
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
                                    },
                                    backgroundColor = SurfaceColor
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
                                    },
                                    backgroundColor = SurfaceColor
                                )
                            }
                        } else {
                            Text(
                                text = "Thank you for your feedback",
                                color = Color(0xFF30D158),
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
}

@Composable
private fun RatingButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    backgroundColor: Color
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
        color = backgroundColor,
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
