package com.example.avaride_1.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.avaride_1.presentation.components.FrostedButton // Removed
// import com.example.avaride_1.presentation.components.FrostedGlassCard // Removed
// import com.example.avaride_1.presentation.components.GlowingMeshGradient // Removed
import com.example.avaride_1.presentation.components.PulsatingOrb
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    // Light Theme Colors
    val PrimaryText = Color(0xFF111827)
    val SecondaryText = Color(0xFF374151)
    val BackgroundColor = Color.White
    val SurfaceColor = Color(0xFFF3F4F6)

    // Debug: Log step changes
    LaunchedEffect(step) {
        println("OnboardingScreen: Step changed to $step")
    }

    // Simulate OTP verification (in production, call real backend)
    fun sendOTP() {
        isVerifying = true
        println("OnboardingScreen: Sending OTP")
        // In production: call backend API to send SMS
        // For demo: auto-advance after 2 seconds
    }

    fun verifyOTP() {
        println("OnboardingScreen: Verifying OTP, code length = ${verificationCode.length}")
        // In production: verify code with backend
        // For demo: accept any 4-digit code
        if (verificationCode.length == 4) {
            step = 3 // Go to QuickInfoStep
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simplified transition to avoid blank screen issues
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(150))
                },
                label = "onboarding_step"
            ) { currentStep ->
                when (currentStep) {
                    0 -> WelcomeSignUpStep(
                        onGetStarted = {
                            println("OnboardingScreen: Get Started clicked, advancing to step 1")
                            step = 1
                        },
                        primaryText = PrimaryText,
                        secondaryText = SecondaryText
                    )
                    1 -> PhoneNumberStep(
                        phoneNumber = phoneNumber,
                        onPhoneChange = { phoneNumber = it },
                        onSendOTP = {
                            sendOTP()
                            step = 2
                        },
                        primaryText = PrimaryText,
                        secondaryText = SecondaryText
                    )
                    2 -> OTPVerificationStep(
                        phoneNumber = phoneNumber,
                        code = verificationCode,
                        onCodeChange = { verificationCode = it },
                        onVerify = { verifyOTP() },
                        onResend = { sendOTP() },
                        primaryText = PrimaryText,
                        secondaryText = SecondaryText
                    )
                    3 -> QuickInfoStep(
                        name = name,
                        onNameChange = { name = it },
                        onComplete = onComplete,
                        primaryText = PrimaryText,
                        secondaryText = SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeSignUpStep(
    onGetStarted: () -> Unit,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🚗",
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to AvaRide",
            color = primaryText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your autonomous ride, seconds away",
            color = secondaryText.copy(alpha = 0.7f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryText, // Dark button
                contentColor = Color.White
            )
        ) {
            Text("Get Started", fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun PhoneNumberStep(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onSendOTP: () -> Unit,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter your mobile number",
            color = primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We'll send you a verification code",
            color = secondaryText.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Singapore phone number input with +65 prefix
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // +65 Prefix (Fixed)
            Text(
                text = "+65",
                color = primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )

            // Phone number input (8 digits)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    // Only allow digits and max 8 characters
                    if (it.all { char -> char.isDigit() } && it.length <= 8) {
                        onPhoneChange(it)
                    }
                },
                placeholder = {
                    Text(
                        "9123 4567",
                        color = secondaryText.copy(alpha = 0.4f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword, // Numeric keypad
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (phoneNumber.length == 8) onSendOTP() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF0A84FF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSendOTP,
            enabled = phoneNumber.length == 8, // Singapore numbers are exactly 8 digits
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryText,
                contentColor = Color.White,
                disabledContainerColor = secondaryText.copy(alpha = 0.2f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text("Continue", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "By continuing, you agree to our Terms & Privacy Policy",
            color = secondaryText.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OTPVerificationStep(
    phoneNumber: String,
    code: String,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter verification code",
            color = primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sent to +65 $phoneNumber",
            color = secondaryText.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                    onCodeChange(it)
                    if (it.length == 4) {
                        // Auto-verify when 4 digits entered
                        println("OTPVerificationStep: Auto-verifying code: $it")
                        onVerify()
                    }
                }
            },
            placeholder = {
                Text(
                    "• • • •",
                    color = secondaryText.copy(alpha = 0.4f),
                    fontSize = 32.sp
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = primaryText,
                unfocusedTextColor = primaryText,
                focusedBorderColor = Color(0xFF0A84FF),
                unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                cursorColor = Color(0xFF0A84FF)
            ),
            textStyle = MaterialTheme.typography.headlineLarge.copy(
                textAlign = TextAlign.Center,
                letterSpacing = 16.sp,
                color = primaryText
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerify,
            enabled = code.length == 4,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryText,
                contentColor = Color.White,
                disabledContainerColor = secondaryText.copy(alpha = 0.2f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text("Verify", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onResend,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Didn't receive code? Resend",
                color = Color(0xFF0A84FF),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun QuickInfoStep(
    name: String,
    onNameChange: (String) -> Unit,
    onComplete: () -> Unit,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Almost there!",
            color = primaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What should we call you?",
            color = secondaryText.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "Your name",
                    color = secondaryText.copy(alpha = 0.4f)
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (name.isNotBlank()) onComplete() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = primaryText,
                unfocusedTextColor = primaryText,
                focusedBorderColor = Color(0xFF0A84FF),
                unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                cursorColor = Color(0xFF0A84FF)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Payment method selection
        Text(
            text = "Payment Method",
            color = secondaryText.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Simple payment card
        Surface(
            onClick = { },
            color = Color(0xFF0A84FF).copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Google Pay",
                        color = primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Quick & secure",
                        color = secondaryText.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = Color(0xFF0A84FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryText,
                contentColor = Color.White,
                disabledContainerColor = secondaryText.copy(alpha = 0.2f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text("Start Riding", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}


// ...existing code (keep WelcomeStep, NameInputStep, PaymentSetupStep, etc.)...
