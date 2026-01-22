package com.example.avaride_1.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.avaride_1.presentation.components.FrostedButton
import com.example.avaride_1.presentation.components.FrostedGlassCard
import com.example.avaride_1.presentation.components.GlowingMeshGradient
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

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingMeshGradient()

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
                    0 -> WelcomeSignUpStep(onGetStarted = {
                        println("OnboardingScreen: Get Started clicked, advancing to step 1")
                        step = 1
                    })
                    1 -> PhoneNumberStep(
                        phoneNumber = phoneNumber,
                        onPhoneChange = { phoneNumber = it },
                        onSendOTP = {
                            sendOTP()
                            step = 2
                        }
                    )
                    2 -> OTPVerificationStep(
                        phoneNumber = phoneNumber,
                        code = verificationCode,
                        onCodeChange = { verificationCode = it },
                        onVerify = { verifyOTP() },
                        onResend = { sendOTP() }
                    )
                    3 -> QuickInfoStep(
                        name = name,
                        onNameChange = { name = it },
                        onComplete = onComplete
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        PulsatingOrb()
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Welcome to",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AvaRide",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "The future of autonomous travel",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NameInputStep(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What's your name?",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "(Optional - tap Continue to skip)",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "Enter your name",
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        FrostedButton(
            text = "Continue",
            onClick = onNext, // Allow continuing without name
            enabled = true, // Always enabled
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PaymentSetupStep(onNext: () -> Unit) {
    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Setup Payment",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose your preferred payment method",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        PaymentMethodCard(
            title = "Google Pay",
            subtitle = "Recommended",
            isRecommended = true,
            onClick = onNext
        )
        Spacer(modifier = Modifier.height(12.dp))
        PaymentMethodCard(
            title = "Credit Card",
            subtitle = "Visa, Mastercard, Amex",
            isRecommended = false,
            onClick = { }
        )
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    subtitle: String,
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isRecommended)
            Color.White.copy(alpha = 0.15f)
        else
            Color.White.copy(alpha = 0.08f),
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
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            if (isRecommended) {
                Surface(
                    color = Color(0xFF30D158).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓",
                        color = Color(0xFF30D158),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Simple payment card for new sign-up flow
@Composable
private fun SimplePaymentCard(
    title: String,
    subtitle: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected)
            Color(0xFF0A84FF).copy(alpha = 0.2f)
        else
            Color.White.copy(alpha = 0.08f),
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
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            if (isSelected) {
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
    }
}

@Composable
private fun BiometricSetupStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        PulsatingOrb(color = Color(0xFF30D158))
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Enable Biometric Auth",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Use fingerprint to unlock your rides securely",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        FrostedButton(
            text = "Enable",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompletionStep(onComplete: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "✨",
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "All Set!",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You're ready to experience the future of travel",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        FrostedButton(
            text = "Get Started",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================================
// SIGN-UP SCREENS - Simple & Fast like Uber/Lyft
// ============================================================================

@Composable
private fun WelcomeSignUpStep(onGetStarted: () -> Unit) {
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
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your autonomous ride, seconds away",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.4f),
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
    onSendOTP: () -> Unit
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
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We'll send you a verification code",
            color = Color.White.copy(alpha = 0.6f),
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
                color = Color.White,
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
                        color = Color.White.copy(alpha = 0.4f)
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
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    cursorColor = Color.White
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
                containerColor = Color.Black.copy(alpha = 0.4f),
                contentColor = Color.White,
                disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text("Continue", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "By continuing, you agree to our Terms & Privacy Policy",
            color = Color.White.copy(alpha = 0.5f),
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
    onResend: () -> Unit
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
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sent to +65 $phoneNumber",
            color = Color.White.copy(alpha = 0.6f),
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
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 32.sp
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color.White
            ),
            textStyle = MaterialTheme.typography.headlineLarge.copy(
                textAlign = TextAlign.Center,
                letterSpacing = 16.sp
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
                containerColor = Color.Black.copy(alpha = 0.4f),
                contentColor = Color.White,
                disabledContainerColor = Color.Black.copy(alpha = 0.2f),
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
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun QuickInfoStep(
    name: String,
    onNameChange: (String) -> Unit,
    onComplete: () -> Unit
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
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What should we call you?",
            color = Color.White.copy(alpha = 0.6f),
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
                    color = Color.White.copy(alpha = 0.4f)
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (name.isNotBlank()) onComplete() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Payment method selection
        Text(
            text = "Payment Method",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Simple payment card
        Surface(
            onClick = { },
            color = Color(0xFF0A84FF).copy(alpha = 0.2f),
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
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Quick & secure",
                        color = Color.White.copy(alpha = 0.6f),
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
                containerColor = Color.Black.copy(alpha = 0.4f),
                contentColor = Color.White,
                disabledContainerColor = Color.Black.copy(alpha = 0.2f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text("Start Riding", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

// ...existing code (keep WelcomeStep, NameInputStep, PaymentSetupStep, etc.)...
