package com.example.avaride_1.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val geminiService = remember { GeminiPredictiveService(BuildConfig.GEMINI_API_KEY) }
    
    // Database & Refs
    val firestoreRepository = remember { com.example.avaride_1.data.repository.FirestoreRepository() }
    val userPrefs = remember { com.example.avaride_1.data.repository.UserPreferencesRepository(context) }
    
    // Session State
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDest by remember { mutableStateOf(Screen.Login.route) }

    // Check session on launch
    LaunchedEffect(Unit) {
        userPrefs.userPhoneNumber.collect { phone ->
            if (!phone.isNullOrBlank()) {
                startDest = Screen.Home.route
            }
            isCheckingSession = false
        }
    }

    if (isCheckingSession) {
        // Loading Splash
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            PulsatingOrb()
        }
        return
    }

    // ViewModels
    val loginViewModel = remember { com.example.avaride_1.presentation.screens.login.LoginViewModel(firestoreRepository, userPrefs) }
    val homeViewModel = remember { HomeViewModel(geminiService) }
    val bookingViewModel = remember { BookingViewModel() }
    val inRideViewModel = remember { InRideViewModel(firestoreRepository, userPrefs) }
    val settingsViewModel = remember { SettingsViewModel(firestoreRepository, userPrefs) }

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
            HomeScreen(
                viewModel = homeViewModel,
                onBookRide = {
                    navController.navigate(Screen.Booking.route)
                },
                onSearchTapped = {
                    // Navigate to destination search
                    navController.navigate(Screen.DestinationSearch.route)
                },
                onQuickDestinationSelected = { name, address, distance ->
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
                     navController.navigate(Screen.BookingStatus.route)
                 },
                 onBack = {
                     navController.popBackStack()
                 }
             )
        }

        composable(Screen.BookingStatus.route) {
            val destination by bookingViewModel.destination.collectAsState()
            
            if (destination != null) {
                com.example.avaride_1.presentation.screens.confirm.BookingStatusScreen(
                    destination = destination!!,
                    onUnlock = {
                        // Clear booking data only after successful unlock/boarding logic starts
                         bookingViewModel.clearBookingData()
                         navController.navigate(Screen.NFCUnlock.route) {
                             popUpTo(Screen.Home.route) { inclusive = false }
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
            viewModel = settingsViewModel
        )
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

