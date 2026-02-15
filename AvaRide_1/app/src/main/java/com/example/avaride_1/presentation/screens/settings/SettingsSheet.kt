package com.example.avaride_1.presentation.screens.settings

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
import com.example.avaride_1.domain.model.PaymentMethod

// Color Palette
private val LightBackground = Color.White
private val LightSurface = Color(0xFFF5F5F7)
private val PrimaryText = Color(0xFF111827)
private val SecondaryText = Color(0xFF222222)
private val AccentBlue = Color(0xFF0A84FF)
private val DestructiveRed = Color(0xFFFF3B30)

@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel,
    isRideActive: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var locationDialogType by remember { mutableStateOf<String?>(null) } // "Home" or "Work"
    var showPaymentDialog by remember { mutableStateOf(false) }

    // Handlers
    if (showHistory) {
        HistoryScreen(
            rideHistory = uiState.rideHistory,
            onBack = { showHistory = false }
        )
        return // Show only history screen
    }

    if (locationDialogType != null) {
        LocationSettingDialog(
            type = locationDialogType!!,
            currentAddress = if (locationDialogType == "Home") uiState.homeLocation else uiState.workLocation,
            onConfirm = { address ->
                viewModel.updateLocation(locationDialogType!!, address)
                locationDialogType = null
            },
            onDismiss = { locationDialogType = null }
        )
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Select Payment Method") },
            text = {
                Column {
                    PaymentMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePaymentMethod(method)
                                    showPaymentDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.paymentMethod == method,
                                onClick = {
                                    viewModel.updatePaymentMethod(method)
                                    showPaymentDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method.name.replace("_", " "))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    .navigationBarsPadding()
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
                        ControlCenterCard(
                            title = "Payment",
                            subtitle = uiState.paymentMethod.name.replace("_", " "),
                            icon = Icons.Default.AccountBox,
                            onClick = { showPaymentDialog = true }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "Home",
                            subtitle = uiState.homeLocation.ifBlank { "Set location" },
                            icon = Icons.Default.Home,
                            onClick = { locationDialogType = "Home" }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "Work",
                            subtitle = uiState.workLocation.ifBlank { "Set location" },
                            icon = Icons.Default.Place,
                            onClick = { locationDialogType = "Work" }
                        )
                    }

                    item {
                        ControlCenterCard(
                            title = "History",
                            subtitle = "${uiState.rideCount} rides",
                            icon = Icons.Default.Star,
                            onClick = { showHistory = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Push logout to bottom

                Button(
                    onClick = {
                        if (!isRideActive) {
                            onLogout()
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
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}
