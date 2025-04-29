package com.senlin.budgetmaster.ui.theme

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
import androidx.compose.ui.graphics.Color // Ensure Color is imported
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define custom light color scheme using the new palette
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = BlueVariant,
    onPrimaryContainer = White,
    secondary = BlueVariant,
    onSecondary = White,
    background = GreyBackground,
    onBackground = GreyText,
    surface = White, // Surfaces like Cards
    onSurface = GreyText,
    error = Color(0xFFB00020), // Standard Material Red for errors
    onError = White,
    surfaceVariant = Color(0xFFE0E0E0), // Slightly darker grey for variants
    onSurfaceVariant = GreyText,
    outline = Color(0xFFBDBDBD) // Grey for outlines
    // Define other colors as needed
)

// Define a basic dark color scheme using the new palette
private val DarkColorScheme = darkColorScheme(
    primary = BlueVariant, // Lighter blue for primary on dark
    onPrimary = White,
    primaryContainer = BluePrimary,
    onPrimaryContainer = White,
    secondary = BlueVariant,
    onSecondary = White,
    background = Color(0xFF121212), // Standard dark background
    onBackground = White,
    surface = Color(0xFF1E1E1E), // Slightly lighter dark for surfaces
    onSurface = White,
    error = Color(0xFFCF6679), // Standard Material dark error color
    onError = Black, // Black text on light error color
    surfaceVariant = Color(0xFF313131),
    onSurfaceVariant = Color(0xFFBDBDBD), // Lighter grey text on dark
    outline = Color(0xFF616161)
    // Define other colors as needed
)

@Composable
fun BudgetMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ - Disabled by default for custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Ensure this else branch exists
    }

    // Status bar color adjustment
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color - consider using background or primary container for less intrusion
            window.statusBarColor = colorScheme.background.toArgb()
            // Set status bar icons light/dark based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography.kt exists and is set up
        content = content
    )
}
