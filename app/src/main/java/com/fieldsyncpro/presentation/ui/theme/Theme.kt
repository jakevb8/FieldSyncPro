package com.fieldsyncpro.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryBlue    = Color(0xFF1565C0)
private val SecondaryTeal  = Color(0xFF00796B)
private val ErrorRed       = Color(0xFFD32F2F)
private val WarningOrange  = Color(0xFFE65100)
private val SuccessGreen   = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary         = PrimaryBlue,
    secondary       = SecondaryTeal,
    error           = ErrorRed,
    background      = Color(0xFFF5F5F5),
    surface         = Color.White,
    onPrimary       = Color.White,
    onSecondary     = Color.White,
    onBackground    = Color(0xFF212121),
    onSurface       = Color(0xFF212121)
)

private val DarkColors = darkColorScheme(
    primary         = Color(0xFF90CAF9),
    secondary       = Color(0xFF80CBC4),
    error           = Color(0xFFEF9A9A),
    background      = Color(0xFF121212),
    surface         = Color(0xFF1E1E1E),
    onPrimary       = Color(0xFF0D47A1),
    onSecondary     = Color(0xFF004D40),
    onBackground    = Color(0xFFEEEEEE),
    onSurface       = Color(0xFFEEEEEE)
)

/** Vibe-specific accent colours for UI chips and indicators. */
val VibeHypeColor   = ErrorRed
val VibeSteadyColor = PrimaryBlue
val VibeChillColor  = SuccessGreen

@Composable
fun FieldSyncProTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}
