package com.batu.simpledarttracker.ui.navigation

import androidx.compose.runtime.Composable

/** iOS has no system back button, so there is nothing to intercept. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
