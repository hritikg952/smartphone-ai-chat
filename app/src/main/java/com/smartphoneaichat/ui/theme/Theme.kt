package com.smartphoneaichat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Material 3 dark color scheme.
 *
 * The app is strictly dark-themed. No light scheme is provided. All surfaces
 * use subtle dark grays instead of pure black (#000) to match Gemini's
 * aesthetic and reduce OLED ghosting.
 */
private val DarkScheme = darkColorScheme(
    // Core
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,

    // Primary accent
    primary = AccentBlue,
    onPrimary = DarkBackground,
    primaryContainer = AccentBlue.copy(alpha = 0.15f),
    onPrimaryContainer = AccentBlue,

    // Secondary
    secondary = AccentGreen,
    onSecondary = DarkBackground,

    // Error
    error = AccentRed,
    onError = DarkBackground,

    // Outline
    outline = AiBubbleBorder
)

/** Apply the app's dark theme around [content]. */
@Composable
fun SmartphoneAIChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = AppTypography,
        content = content
    )
}