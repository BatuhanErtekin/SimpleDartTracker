package com.batu.simpledarttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The app always runs the dark "arena" theme so it matches the logo, splash and game board.
private val ArenaColors = darkColorScheme(
    primary = Brand.Spruce,
    onPrimary = Brand.Chalk,
    primaryContainer = Color(0xFF1C3B2B),
    onPrimaryContainer = Brand.Chalk,
    secondary = Brand.Bull,
    onSecondary = Brand.Chalk,
    secondaryContainer = Color(0xFF3B1712),
    onSecondaryContainer = Brand.Chalk,
    background = Brand.Slate,
    onBackground = Brand.Chalk,
    surface = Brand.Slate2,
    onSurface = Brand.Chalk,
    surfaceVariant = Color(0xFF20302A),
    onSurfaceVariant = Brand.Wire,
    outline = Brand.Wire,
    outlineVariant = Color(0xFF2A3A32),
    error = Brand.Bull,
    onError = Brand.Chalk,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArenaColors,
        content = content,
    )
}
