package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StravaOrange,
    onPrimary = Color.White,
    primaryContainer = StravaOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = GoldStar,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382A00),
    onSecondaryContainer = GoldStar,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = StravaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = StravaOrangeDark,
    secondary = GoldStar,
    onSecondary = Color.Black,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCCCCCC)
)

@Composable
fun BhagwaTheme(
    darkTheme: Boolean = true, // Default to sleek dark athletic theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BhagwaTheme(darkTheme = darkTheme, content = content)
}

