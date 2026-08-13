package com.batu.simpledarttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The app always runs dark so it matches the logo, the splash and the boards.
//
// The surfaceContainer roles matter as much as the obvious ones: Material 3 tints cards, menus
// and drawers from them, so anything left undefined falls back to Material's own purple-grey
// and looks foreign next to the brand colours.
private val ArenaColors = darkColorScheme(
    primary = Brand.Green,
    onPrimary = Brand.Ink,
    primaryContainer = Brand.GreenDeep,
    onPrimaryContainer = Brand.Chalk,
    secondary = Brand.Red,
    onSecondary = Brand.Ink,
    secondaryContainer = Brand.RedDeep,
    onSecondaryContainer = Brand.Chalk,
    tertiary = Brand.Amber,
    onTertiary = Brand.Ink,
    tertiaryContainer = Brand.AmberDeep,
    onTertiaryContainer = Brand.Chalk,
    background = Brand.Slate,
    onBackground = Brand.Chalk,
    surface = Brand.Slate2,
    onSurface = Brand.Chalk,
    surfaceVariant = Color(0xFF1D2521),
    onSurfaceVariant = Brand.Wire,
    surfaceDim = Brand.Slate,
    surfaceBright = Color(0xFF2A332E),
    surfaceContainerLowest = Color(0xFF090D0B),
    surfaceContainerLow = Brand.Slate2,
    surfaceContainer = Color(0xFF1B221E),
    surfaceContainerHigh = Color(0xFF1E2622),
    surfaceContainerHighest = Brand.Key,
    inverseSurface = Brand.Chalk,
    inverseOnSurface = Brand.Slate,
    outline = Brand.Wire,
    outlineVariant = Color(0xFF2C3833),
    scrim = Color(0xFF000000),
    error = Brand.Red,
    onError = Brand.Ink,
    errorContainer = Brand.RedDeep,
    onErrorContainer = Brand.Chalk,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArenaColors,
        content = content,
    )
}
