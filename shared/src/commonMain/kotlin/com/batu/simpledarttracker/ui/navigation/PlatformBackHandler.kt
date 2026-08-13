package com.batu.simpledarttracker.ui.navigation

import androidx.compose.runtime.Composable

/**
 * Handles the platform's own "go back" gesture while [enabled].
 *
 * Android routes its system back button here so it walks our back stack instead of leaving the
 * app. iOS has no equivalent system-wide gesture, so there it does nothing — screens that need
 * to be dismissable on iOS must offer their own control.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
