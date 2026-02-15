package com.example.avaride_1.presentation.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette
private val LightBackground = Color.White
private val LightSurface = Color(0xFFF5F5F7)
private val PrimaryText = Color(0xFF111827) // Dark Grey
private val SecondaryText = Color(0xFF222222) // Slightly Lighter Dark Grey
private val AccentBlue = Color(0xFF0A84FF)
private val AccentGreen = Color(0xFF30D158)
private val DestructiveRed = Color(0xFFFF3B30)

@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel,
    isRideActive: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .blur(20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
                .clickable(enabled = false) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(LightBackground)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Settings",
                            color = PrimaryText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.userName,
                            color = SecondaryText.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(LightSurface, androidx.compose.foundation.shape.CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PrimaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ControlCenterToggle(
                            title = "Quiet Mode",
                            subtitle = if (uiState.quietMode) "On" else "Off",
                            icon = Icons.Default.Notifications,
                            isActive = uiState.quietMode,
                            onClick = { viewModel.toggleQuietMode() }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "Payment",
                            subtitle = uiState.paymentMethod.name.replace("_", " "),
                            icon = Icons.Default.AccountBox,
                            onClick = { }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "Home",
                            subtitle = "Set location",
                            icon = Icons.Default.Home,
                            onClick = { }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "Work",
                            subtitle = "Set location",
                            icon = Icons.Default.Place,
                            onClick = { }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "History",
                            subtitle = "${uiState.rideCount} rides",
                            icon = Icons.Default.Star,
                            onClick = { }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "Support",
                            subtitle = "Get help",
                            icon = Icons.Default.Info,
                            onClick = { }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (!isRideActive) {
                            viewModel.logout()
                            onDismiss()
                        }
                    },
                    enabled = !isRideActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRideActive) Color.Gray else DestructiveRed,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isRideActive) "Cannot Log Out During Ride" else "Log Out",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                QuickSettingsList(
                    preferences = uiState.preferences,
                    onPreferenceChange = { key, value ->
                        viewModel.updatePreference(key, value)
                    }
                )
            }
        }
    }
}

@Composable
private fun ControlCenterToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isActive) AccentBlue.copy(alpha = 0.1f) else LightSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) AccentBlue else PrimaryText.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = SecondaryText.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ControlCenterCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = LightSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryText.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = SecondaryText.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun QuickSettingsList(
    preferences: Map<String, Boolean>,
    onPreferenceChange: (String, Boolean) -> Unit
) {
    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Settings",
                color = SecondaryText.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            preferences.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = key.replace("_", " ").replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        },
                        color = PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Switch(
                        checked = value,
                        onCheckedChange = { onPreferenceChange(key, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}
