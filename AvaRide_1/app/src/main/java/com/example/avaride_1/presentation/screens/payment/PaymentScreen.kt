package com.example.avaride_1.presentation.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed for Light Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    totalFare: Double,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Google Pay") }
    var isProcessing by remember { mutableStateOf(false) }

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    val SurfaceColor = Color(0xFFF3F4F6)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = BackgroundColor,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Payment",
                        color = PrimaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Total Amount
                Text(
                    text = "Total Amount",
                    color = SecondaryText,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${String.format("%.2f", totalFare)}",
                    color = PrimaryText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Payment Methods
                Text(
                    text = "Payment Method",
                    color = PrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                PaymentMethodItem(
                    name = "Google Pay",
                    icon = Icons.Default.AccountBox, // Placeholder icon
                    isSelected = selectedMethod == "Google Pay",
                    onClick = { selectedMethod = "Google Pay" },
                    primaryText = PrimaryText,
                    surfaceColor = SurfaceColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                PaymentMethodItem(
                    name = "Credit Card •••• 4242",
                    icon = Icons.Default.AccountBox,
                    isSelected = selectedMethod == "Credit Card",
                    onClick = { selectedMethod = "Credit Card" },
                    primaryText = PrimaryText,
                    surfaceColor = SurfaceColor
                )

                Spacer(modifier = Modifier.weight(1f))

                // Pay Button
                Button(
                    onClick = {
                        isProcessing = true
                        // Simulate payment processing
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isProcessing = false
                            onPaymentSuccess()
                        }, 2000)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF30D158),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing...")
                    } else {
                        Text(
                            text = "Pay & Book Ride",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryText: Color,
    surfaceColor: Color
) {
    Surface(
        color = if (isSelected) Color(0xFF0A84FF).copy(alpha = 0.1f) else surfaceColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF0A84FF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF0A84FF) else primaryText.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                color = if (isSelected) Color(0xFF0A84FF) else primaryText,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
