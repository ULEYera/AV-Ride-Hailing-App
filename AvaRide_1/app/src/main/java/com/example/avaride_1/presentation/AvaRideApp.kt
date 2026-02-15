package com.example.avaride_1.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.avaride_1.BuildConfig
import com.example.avaride_1.data.remote.GeminiPredictiveService
import com.example.avaride_1.presentation.components.GlowingMeshGradient
import com.example.avaride_1.presentation.components.PulsatingOrb
import com.example.avaride_1.presentation.navigation.Screen
import com.example.avaride_1.presentation.screens.home.HomeScreen
import com.example.avaride_1.presentation.screens.home.HomeViewModel
import com.example.avaride_1.presentation.screens.booking.BookingViewModel
import com.example.avaride_1.presentation.screens.inride.InRideScreen
import com.example.avaride_1.presentation.screens.inride.InRideViewModel
import com.example.avaride_1.presentation.screens.nfc.NFCUnlockScreen
import com.example.avaride_1.presentation.screens.onboarding.OnboardingScreen
import com.example.avaride_1.presentation.screens.settings.SettingsSheet
import com.example.avaride_1.presentation.screens.settings.SettingsViewModel
import com.example.avaride_1.presentation.screens.summary.RideSummaryScreen

/**
 * Main app navigation and state manager
 * Orchestrates the "Zero-UI" flow between screens
 */
@Composable
fun AvaRideApp() {
    val navController = rememberNavController()
    var showSettings by remember { mutableStateOf(false) }

    // Initialize services
    // Services & Repositories
    // Services & Repositories
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val geminiService = remember { GeminiPredictiveService(BuildConfig.GEMINI_API_KEY) }
    
    // Database & Refs
    val firestoreRepository = remember { com.example.avaride_1.data.repository.FirestoreRepository() }
    val userPrefs = remember { com.example.avaride_1.data.repository.UserPreferencesRepository(context) }
    
    // ViewModels
    val loginViewModel = remember { com.example.avaride_1.presentation.screens.login.LoginViewModel(firestoreRepository, userPrefs) }
    val homeViewModel = remember { HomeViewModel(geminiService, firestoreRepository, userPrefs) }
    val bookingViewModel = remember { BookingViewModel() }
    val inRideViewModel = remember { InRideViewModel(firestoreRepository, userPrefs) }
    val settingsViewModel = remember { SettingsViewModel(firestoreRepository, userPrefs) }

    // Session State
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDest by remember { mutableStateOf(Screen.Login.route) }

    // Check session on launch
    LaunchedEffect(Unit) {
        userPrefs.userPhoneNumber
            .distinctUntilChanged()
            .collect { phone ->
            println("SESSION_DEBUG: Phone state changed: $phone")
            if (isCheckingSession) {
                if (!phone.isNullOrBlank()) {
                    println("SESSION_DEBUG: Found existing session, starting at Home")
                    startDest = Screen.Home.route
                } else {
                    println("SESSION_DEBUG: No session found, starting at Login")
                }
                isCheckingSession = false
            } else {
                // Runtime check (e.g. after logout)
                if (phone.isNullOrBlank()) {
                    println("SESSION_DEBUG: Session cleared, navigating to Login")
                    // Reset ViewModels if needed
                    bookingViewModel.clearBookingData()
                    
                    try {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        println("SESSION_DEBUG: Navigation failed: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    if (isCheckingSession) {
        // Loading Splash
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            PulsatingOrb()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Screen.Login.route) {
            com.example.avaride_1.presentation.screens.login.LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            // Deprecated/skipped if Login is used, or can flow from Login -> Onboarding -> Home
            // For now, let's keep it simple: Login -> Home
             OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val isRideActive by bookingViewModel.isRideActive.collectAsState()
            
            // Simple alert state (should ideally use a Dialog composable)
            var showActiveRideAlert by remember { mutableStateOf(false) }

            if (showActiveRideAlert) {
                AlertDialog(
                    onDismissRequest = { showActiveRideAlert = false },
                    title = { Text("Ride in Progress") },
                    text = { Text("You already have an active ride. Please complete it before booking another.") },
                    confirmButton = {
                        TextButton(onClick = { 
                            showActiveRideAlert = false
                            navController.navigate(Screen.BookingStatus.route)
                        }) {
                            Text("View Ride")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showActiveRideAlert = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            HomeScreen(
                viewModel = homeViewModel,
                onBookRide = {
                    if (isRideActive) {
                        showActiveRideAlert = true
                    } else {
                        navController.navigate(Screen.Booking.route)
                    }
                },
                onSearchTapped = {
                    if (isRideActive) {
                        showActiveRideAlert = true
                    } else {
                        navController.navigate(Screen.DestinationSearch.route)
                    }
                },
                onQuickDestinationSelected = { name, address, distance ->
                    if (isRideActive) {
                        showActiveRideAlert = true
                    } else {
                        // Create destination from quick access button
                        val quickDestination = com.example.avaride_1.presentation.screens.search.SearchLocation(
                            name = name,
                            address = address,
                            distance = distance,
                            latitude = 1.4010,
                            longitude = 103.9070
                        )
                        // Save to ViewModel instead of savedStateHandle
                        bookingViewModel.setDestination(quickDestination)
                        navController.navigate(Screen.PickupSelection.route)
                    }
                },
                onProfileTapped = {
                    showSettings = true
                }
            )
        }

        composable(Screen.DestinationSearch.route) {
            com.example.avaride_1.presentation.screens.search.DestinationSearchScreen(
                onDestinationSelected = { destination ->
                    // Save to ViewModel
                    bookingViewModel.setDestination(destination)
                    navController.navigate(Screen.PickupSelection.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PickupSelection.route) {
            // Get destination from ViewModel
            val destination by bookingViewModel.destination.collectAsState()

            if (destination != null) {
                com.example.avaride_1.presentation.screens.pickup.PickupSelectionScreen(
                    destination = destination!!,
                    onPickupSelected = { pickup ->
                        println("PickupSelection: Pickup selected - ${pickup.name}")
                        println("PickupSelection: Destination - ${destination!!.name}")
                        // Save pickup to ViewModel
                        bookingViewModel.setPickup(pickup)
                        println("PickupSelection: Data saved to ViewModel")
                        // Navigate to confirmation
                        println("PickupSelection: Navigating to BookingConfirmation")
                        navController.navigate(Screen.BookingConfirmation.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                // Fallback if destination is missing
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlowingMeshGradient()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Destination data missing",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }

        composable(Screen.BookingConfirmation.route) {
            // Get destination and pickup from ViewModel
            val destination by bookingViewModel.destination.collectAsState()
            val pickup by bookingViewModel.pickup.collectAsState()

            println("BookingConfirmation: destination=$destination, pickup=$pickup")

            if (destination != null && pickup != null) {
                com.example.avaride_1.presentation.screens.confirm.BookingConfirmationScreen(
                    pickup = pickup!!,
                    destination = destination!!,
                    onConfirm = {
                        // Navigate to Payment instead of clearing data
                        navController.navigate(Screen.Payment.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                // Fallback: Show error and go back
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlowingMeshGradient()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Booking data missing",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Destination: ${if (destination == null) "Missing ❌" else "OK ✅"}\nPickup: ${if (pickup == null) "Missing ❌" else "OK ✅"}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        androidx.compose.material3.Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }

        composable(Screen.Payment.route) {
             com.example.avaride_1.presentation.screens.payment.PaymentScreen(
                 totalFare = 12.50, // Hardcoded for MVP
                 onPaymentSuccess = {
                     bookingViewModel.setRideActive(true)
                     navController.navigate(Screen.BookingStatus.route)
                 },
                 onBack = {
                     navController.popBackStack()
                 }
             )
        }

        composable(Screen.BookingStatus.route) {
            val destination by bookingViewModel.destination.collectAsState()
            val arrivalTime by bookingViewModel.arrivalTime.collectAsState()
            
            if (destination != null) {
                com.example.avaride_1.presentation.screens.confirm.BookingStatusScreen(
                    destination = destination!!,
                    arrivalTime = arrivalTime,
                    onUnlock = {
                        // Clear booking data only after successful unlock/boarding logic starts
                         bookingViewModel.clearBookingData()
                         navController.navigate(Screen.NFCUnlock.route) {
                             popUpTo(Screen.Home.route) { inclusive = false }
                         }
                    },
                    onBack = {
                        // Go back to Home explicitly as per request
                        navController.navigate(Screen.Home.route) {
                             popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            } else {
                 // Fallback if destination lost (shouldn't happen in single session)
                 navController.popBackStack()
            }
        }

        composable(Screen.Booking.route) {
            // Booking screen would show AR wayfinding and vehicle tracking
            // For now, simulate booking flow
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                navController.navigate(Screen.NFCUnlock.route)
            }

            BookingPlaceholder(
                onContinue = {
                    navController.navigate(Screen.NFCUnlock.route)
                }
            )
        }

        composable(Screen.NFCUnlock.route) {
            com.example.avaride_1.presentation.screens.nfc.NFCUnlockScreen(
                onUnlocked = {
                    navController.navigate(Screen.InRide.route)
                },
                onSkip = {
                    // Allow skipping if NFC not available (demo mode)
                    navController.navigate(Screen.InRide.route)
                }
            )
        }

        composable(Screen.InRide.route) {
            InRideScreen(
                viewModel = inRideViewModel,
                onEmergencyStop = {
                    // Handle emergency stop
                }
            )

            // Simulate ride completion based on ViewModel state
            val uiState by inRideViewModel.uiState.collectAsState()
            
            LaunchedEffect(uiState.isRideComplete) {
                if (uiState.isRideComplete) {
                    // Small delay for UX
                    kotlinx.coroutines.delay(1000)
                    navController.navigate(Screen.RideSummary.route)
                }
            }
        }

        composable(Screen.RideSummary.route) {
            RideSummaryScreen(
                totalCost = 12.50,
                destination = "Home",
                onRatingSubmitted = { isPositive ->
                    // Handle rating
                },
                onDismiss = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }

    // Settings overlay
    if (showSettings) {
        SettingsSheet(
            onDismiss = { showSettings = false },
            onLogout = {
                // Explicit logout sequence
                settingsViewModel.logout()
                bookingViewModel.clearBookingData()
                loginViewModel.resetState() // Clear isLoggedIn so Login screen doesn't auto-redirect
                showSettings = false
                
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            viewModel = settingsViewModel,
            isRideActive = bookingViewModel.isRideActive.collectAsState().value
        )
    }

    // Floating Ride Widget (Only show if active ride & not on ride screens)
    val isRideActive by bookingViewModel.isRideActive.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)
    val currentRoute = navBackStackEntry?.destination?.route
    val arrivalTime by bookingViewModel.arrivalTime.collectAsState()

    if (isRideActive && 
        currentRoute != Screen.BookingStatus.route && 
        currentRoute != Screen.NFCUnlock.route && 
        currentRoute != Screen.InRide.route) {
        
        // Calculate remaining seconds for widget
        var remainingSeconds by remember { mutableStateOf(0L) }
        
        LaunchedEffect(arrivalTime) {
            if (arrivalTime != null) {
                while (true) {
                    val remaining = (arrivalTime!! - System.currentTimeMillis()) / 1000
                    remainingSeconds = if (remaining > 0) remaining else 0
                    delay(1000)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // Check formatting
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingRidePill(
                remainingSeconds = remainingSeconds,
                onClick = { navController.navigate(Screen.BookingStatus.route) }
            )
        }
    }
}

@Composable
fun FloatingRidePill(
    remainingSeconds: Long,
    onClick: () -> Unit
) {
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeText = if (remainingSeconds > 0) "Arriving in ${String.format("%d:%02d", mins, secs)}" else "Arrived!"
    val color = if (remainingSeconds > 0) Color.White else Color(0xFF30D158)

    androidx.compose.material3.Surface(
        onClick = onClick,
        color = Color(0xFF1C1C1E),
        shape = androidx.compose.foundation.shape.CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
        modifier = Modifier
            .height(56.dp)
            .widthIn(min = 200.dp)
            .padding(horizontal = 16.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
             Text(
                text = "🚗",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = timeText,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to view",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}


// Placeholder screens (to be fully implemented)
@Composable
private fun BookingPlaceholder(onContinue: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlowingMeshGradient()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PulsatingOrb()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Finding your vehicle...",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

