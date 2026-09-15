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
    primary = Brand.Gold,
    onPrimary = Brand.Ink,
    primaryContainer = Brand.GoldDeep,
    onPrimaryContainer = Brand.Chalk,
    secondary = Brand.Ember,
    onSecondary = Brand.Ink,
    secondaryContainer = Brand.EmberDeep,
    onSecondaryContainer = Brand.Chalk,
    tertiary = Brand.Honey,
    onTertiary = Brand.Ink,
    tertiaryContainer = Brand.HoneyDeep,
    onTertiaryContainer = Brand.Chalk,
    background = Brand.Night,
    onBackground = Brand.Chalk,
    surface = Brand.Panel,
    onSurface = Brand.Chalk,
    surfaceVariant = Color(0xFF221C13),
    onSurfaceVariant = Brand.Wire,
    surfaceDim = Brand.Night,
    surfaceBright = Color(0xFF31281B),
    surfaceContainerLowest = Color(0xFF0B0906),
    surfaceContainerLow = Brand.Panel,
    surfaceContainer = Color(0xFF1F1911),
    surfaceContainerHigh = Color(0xFF231C13),
    surfaceContainerHighest = Brand.Key,
    inverseSurface = Brand.Chalk,
    inverseOnSurface = Brand.Night,
    outline = Brand.Wire,
    outlineVariant = Brand.Edge,
    scrim = Color(0xFF000000),
    error = Brand.Ember,
    onError = Brand.Ink,
    errorContainer = Brand.EmberDeep,
    onErrorContainer = Brand.Chalk,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArenaColors,
        content = content,
    )
}
