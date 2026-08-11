package com.batu.simpledarttracker.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.ui.brand.DartMark
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.app_name
import simpledarttracker.shared.generated.resources.welcome_continue_guest
import simpledarttracker.shared.generated.resources.welcome_google_signin
import simpledarttracker.shared.generated.resources.welcome_tagline

/**
 * Entry screen. "Sign in with Google" will land here later; for now the only way
 * forward is to continue without an account.
 */
@Composable
fun WelcomeScreen(
    onContinueAsGuest: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DartMark(
            lineColor = MaterialTheme.colorScheme.onBackground,
            greenColor = MaterialTheme.colorScheme.primary,
            redColor = MaterialTheme.colorScheme.secondary,
            haloColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.size(96.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.welcome_tagline),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(48.dp))

        // Disabled for now — Google sign-in comes later.
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.welcome_google_signin))
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.welcome_continue_guest))
        }
    }
}
