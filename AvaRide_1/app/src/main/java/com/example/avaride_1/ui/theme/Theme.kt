package com.example.avaride_1.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Apple Intelligence / iOS-inspired color palette
private val AvaRideDarkScheme = darkColorScheme(
    primary = Color(0xFF0A84FF), // iOS blue
    secondary = Color(0xFF5E5CE6), // iOS purple
    tertiary = Color(0xFF30D158), // iOS green
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF3B30) // iOS red
)

@Composable
fun AvaRide_1Theme(
    darkTheme: Boolean = true, // Always use dark theme for "Apple Intelligence" aesthetic
    dynamicColor: Boolean = false, // Disable dynamic color to maintain brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = AvaRideDarkScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}