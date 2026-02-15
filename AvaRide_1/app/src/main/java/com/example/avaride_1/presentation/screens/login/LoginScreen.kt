package com.example.avaride_1.presentation.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed for Light Theme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    // val SurfaceColor = Color(0xFFF3F4F6) // Not strictly needed if we remove the card

    // Effect to navigate on success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AvaRide",
                color = PrimaryText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Autonomous Sign-In",
                color = SecondaryText.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (uiState.isNewUser) {
                // Name Input Step
                Text(
                    text = "Welcome to AvaRide!",
                    color = PrimaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "What should we call you?",
                    color = SecondaryText.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Your Name", color = SecondaryText) },
                    placeholder = { Text("e.g. John Doe", color = SecondaryText.copy(alpha = 0.4f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedBorderColor = SecondaryText.copy(alpha=0.3f),
                        cursorColor = Color(0xFF0A84FF)
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.completeRegistration() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText, // Dark button for contrast
                        contentColor = Color.White
                    ),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                     if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Start Riding", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
            } else if (!uiState.isOtpSent) {
                // Phone Number Step
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    label = { Text("Phone Number", color = SecondaryText) },
                    placeholder = { Text("e.g. 8888 8888", color = SecondaryText.copy(alpha = 0.4f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedBorderColor = SecondaryText.copy(alpha=0.3f),
                        cursorColor = Color(0xFF0A84FF)
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.sendOtp() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryText, // Dark button for contrast
                        contentColor = Color.White
                    ),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // OTP Step
                Text(
                    text = "Enter code sent to ${uiState.phoneNumber}",
                    color = SecondaryText.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.otpCode,
                    onValueChange = { viewModel.onOtpCodeChange(it) },
                    label = { Text("4-Digit Code", color = SecondaryText) },
                    placeholder = { Text("Any code works for demo", color = SecondaryText.copy(alpha = 0.4f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedBorderColor = SecondaryText.copy(alpha=0.3f),
                        cursorColor = Color(0xFF0A84FF)
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.verifyOtp() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF30D158), // Green for success/verify
                        contentColor = Color.White
                    ),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify & Enter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { viewModel.onPhoneNumberChange("") }) { // Reset to start
                    Text("Change Number", color = Color(0xFF0A84FF))
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}
