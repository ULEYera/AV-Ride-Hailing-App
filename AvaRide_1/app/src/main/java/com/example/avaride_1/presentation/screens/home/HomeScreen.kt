package com.example.avaride_1.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avaride_1.presentation.components.FrostedButton
import com.example.avaride_1.presentation.components.FrostedGlassCard
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.components.PulsatingOrb

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBookRide: () -> Unit,
    onSearchTapped: () -> Unit,
    onQuickDestinationSelected: (String, String, String) -> Unit, // name, address, distance
    onProfileTapped: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ProfileAvatar(onClick = onProfileTapped)
            }

            Spacer(modifier = Modifier.weight(1f))

            when (uiState) {
                is HomeUiState.Loading -> LoadingState()
                is HomeUiState.Prediction -> PredictiveDestinationCard(
                    state = uiState as HomeUiState.Prediction,
                    onBookRide = onBookRide
                )
                is HomeUiState.NoPrediction -> ManualInputCard(
                    onSearchTapped = onSearchTapped,
                    onQuickDestinationSelected = onQuickDestinationSelected
                )
                is HomeUiState.Error -> ErrorState(onRetry = { viewModel.loadPrediction() })
            }

            Spacer(modifier = Modifier.weight(1f))
            // SearchPill removed - redundant as we have destination input above
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileAvatar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "JD",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        PulsatingOrb()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Reading your context...",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PredictiveDestinationCard(
    state: HomeUiState.Prediction,
    onBookRide: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Text(
            text = "Heading ${state.destination.name}?",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.destination.address,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RideDetailChip(label = "Standard AV", icon = "🚗")
            RideDetailChip(label = "${state.etaMinutes} mins", icon = "⏱️")
            RideDetailChip(label = "$${String.format("%.2f", state.estimatedPrice)}", icon = "💳")
        }

        Spacer(modifier = Modifier.height(32.dp))

        FrostedButton(
            text = "Book Ride",
            onClick = {
                isPressed = true
                onBookRide()
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.destination.confidence > 0.7f) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✨ High confidence prediction",
                color = Color(0xFF30D158).copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RideDetailChip(label: String, icon: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "Unable to predict destination",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        FrostedButton(
            text = "Try Again",
            onClick = onRetry
        )
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = Color.Black.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search for a place",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ManualInputCard(
    onSearchTapped: () -> Unit,
    onQuickDestinationSelected: (String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Where to?",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Let's get you moving",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Destination Input ONLY (pickup comes after destination selection)
        Surface(
            onClick = onSearchTapped,
            color = Color.Black.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enter destination",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Where do you want to go?",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick suggestions
        Text(
            text = "Quick Access",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickDestinationChip(
                icon = "🏠",
                label = "Home",
                subtitle = "Punggol 531174",
                onClick = {
                    onQuickDestinationSelected("Blk 174 Punggol Field", "Singapore 531174", "12.5 km")
                },
                modifier = Modifier.weight(1f)
            )
            QuickDestinationChip(
                icon = "💼",
                label = "Work",
                subtitle = "SIT Punggol",
                onClick = {
                    onQuickDestinationSelected("SIT Punggol Campus", "10 Dover Drive, Singapore 138683", "13.2 km")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Additional quick destinations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickDestinationChip(
                icon = "🛍️",
                label = "Mall",
                subtitle = "Waterway Point",
                onClick = {
                    onQuickDestinationSelected("Waterway Point", "83 Punggol Central, Singapore 828761", "13.8 km")
                },
                modifier = Modifier.weight(1f)
            )
            QuickDestinationChip(
                icon = "🏥",
                label = "Hospital",
                subtitle = "Nearest",
                onClick = {
                    onQuickDestinationSelected("National University Hospital", "5 Lower Kent Ridge Road, Singapore 119074", "22.0 km")
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickDestinationChip(
    icon: String,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
