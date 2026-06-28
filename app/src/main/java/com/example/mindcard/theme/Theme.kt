package com.example.mindcard.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// === BRAND COLORS ===
val PrimaryIndigo = Color(0xFF6C63FF)
val PrimaryIndigoDark = Color(0xFF8B83FF)
val SecondaryGreen = Color(0xFF00D68F)
val AccentYellow = Color(0xFFFFD93D)
val ErrorRed = Color(0xFFFF6B6B)

// === LIGHT THEME ===
private val LightPrimary = Color(0xFF6C63FF)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFE8E5FF)
private val LightOnPrimaryContainer = Color(0xFF1E00A8)
private val LightSecondary = Color(0xFF00D68F)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFF8F9FE)
private val LightOnBackground = Color(0xFF1A1C2E)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1A1C2E)
private val LightSurfaceVariant = Color(0xFFF0F1F8)
private val LightOnSurfaceVariant = Color(0xFF6B7280)
private val LightOutline = Color(0xFFE5E7EB)
private val LightError = Color(0xFFFF6B6B)

// === DARK THEME - Beautiful & Modern ===
private val DarkPrimary = Color(0xFF9D96FF)
private val DarkOnPrimary = Color(0xFF1E00A8)
private val DarkPrimaryContainer = Color(0xFF3D3580)
private val DarkOnPrimaryContainer = Color(0xFFE8E5FF)
private val DarkSecondary = Color(0xFF00FFB8)
private val DarkOnSecondary = Color(0xFF003D2A)
private val DarkBackground = Color(0xFF0D0F1A)
private val DarkOnBackground = Color(0xFFE8E9F0)
private val DarkSurface = Color(0xFF161829)
private val DarkOnSurface = Color(0xFFE8E9F0)
private val DarkSurfaceVariant = Color(0xFF1E2035)
private val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
private val DarkOutline = Color(0xFF2D3050)
private val DarkError = Color(0xFFFF8A80)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
)

@Composable
fun MindCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
