package com.batu.simpledarttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The app always runs the dark "arena" theme so it matches the logo, splash and game board.
//
// The surfaceContainer roles matter as much as the obvious ones: Material 3 tints cards, menus
// and drawers from them, so anything left undefined falls back to Material's own purple-grey
// and looks foreign next to the brand colours.
private val ArenaColors = darkColorScheme(
    primary = Brand.Spruce,
    onPrimary = Brand.Chalk,
    primaryContainer = Color(0xFF1C3B2B),
    onPrimaryContainer = Brand.Chalk,
    secondary = Brand.Bull,
    onSecondary = Brand.Chalk,
    secondaryContainer = Color(0xFF3B1712),
    onSecondaryContainer = Brand.Chalk,
    tertiary = Brand.Spruce,
    onTertiary = Brand.Chalk,
    tertiaryContainer = Color(0xFF1C3B2B),
    onTertiaryContainer = Brand.Chalk,
    background = Brand.Slate,
    onBackground = Brand.Chalk,
    surface = Brand.Slate2,
    onSurface = Brand.Chalk,
    surfaceVariant = Color(0xFF20302A),
    onSurfaceVariant = Brand.Wire,
    surfaceDim = Brand.Slate,
    surfaceBright = Color(0xFF2A3A32),
    surfaceContainerLowest = Color(0xFF0A100D),
    surfaceContainerLow = Brand.Slate2,
    surfaceContainer = Color(0xFF182320),
    surfaceContainerHigh = Color(0xFF1E2B26),
    surfaceContainerHighest = Brand.Key,
    inverseSurface = Brand.Chalk,
    inverseOnSurface = Brand.Slate,
    outline = Brand.Wire,
    outlineVariant = Color(0xFF2A3A32),
    scrim = Color(0xFF000000),
    error = Brand.Bull,
    onError = Brand.Chalk,
    errorContainer = Color(0xFF3B1712),
    onErrorContainer = Brand.Chalk,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArenaColors,
        content = content,
    )
}
