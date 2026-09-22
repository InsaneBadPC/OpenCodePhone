package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OpenCodeDarkColorScheme = darkColorScheme(
    primary = CyanBright,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = CyanBright,
    secondary = EmeraldBright,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = EmeraldBright,
    tertiary = VioletPurple,
    onTertiary = Color.White,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate200,
    outline = Slate700,
    outlineVariant = Slate800,
    error = RoseError,
    onError = Color.White
)

private val OpenCodeLightColorScheme = lightColorScheme(
    primary = CyanAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF0E7490),
    secondary = EmeraldSuccess,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF047857),
    tertiary = IndigoAccent,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    outlineVariant = Slate400,
    error = RoseError,
    onError = Color.White
)

@Composable
fun OpenCodeTheme(
    darkTheme: Boolean = true, // Default to sleek IDE dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OpenCodeDarkColorScheme else OpenCodeLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    OpenCodeTheme(darkTheme = true, content = content)
}
