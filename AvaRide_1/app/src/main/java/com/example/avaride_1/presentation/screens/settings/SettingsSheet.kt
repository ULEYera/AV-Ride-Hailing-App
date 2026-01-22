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

@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
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
                    .background(Color(0xFF1C1C1E))
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
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.userName,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
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
        color = if (isActive) Color(0xFF0A84FF).copy(alpha = 0.3f) else Color(0xFF2C2C2E),
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
                tint = if (isActive) Color(0xFF0A84FF) else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
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
        color = Color(0xFF2C2C2E),
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
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
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
        color = Color(0xFF2C2C2E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Settings",
                color = Color.White.copy(alpha = 0.6f),
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
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    Switch(
                        checked = value,
                        onCheckedChange = { onPreferenceChange(key, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF30D158),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF39393D)
                        )
                    )
                }
            }
        }
    }
}
