package com.example.avaride_1.presentation

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
    val geminiService = remember {
        GeminiPredictiveService(BuildConfig.GEMINI_API_KEY)
    }

    // ViewModels (in production, use Hilt/Koin for DI)
    val homeViewModel = remember { HomeViewModel(geminiService) }
    val bookingViewModel = remember { BookingViewModel() }
    val inRideViewModel = remember { InRideViewModel() }
    val settingsViewModel = remember { SettingsViewModel() }

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
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
                        // Clear booking data after confirmation
                        bookingViewModel.clearBookingData()
                        navController.navigate(Screen.NFCUnlock.route) {
                            // Clear the stack up to home
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
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

            // Simulate ride completion
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(10000)
                navController.navigate(Screen.RideSummary.route)
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

