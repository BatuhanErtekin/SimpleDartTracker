package com.batu.simpledarttracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.batu.simpledarttracker.domain.model.GameMode
import com.batu.simpledarttracker.ui.game.GameScreen
import com.batu.simpledarttracker.ui.home.HomeScreen
import com.batu.simpledarttracker.ui.navigation.Screen
import com.batu.simpledarttracker.ui.splash.SplashScreen
import com.batu.simpledarttracker.ui.theme.AppTheme
import com.batu.simpledarttracker.ui.welcome.WelcomeScreen
import com.batu.simpledarttracker.ui.x01.X01ScoreScreen

@Composable
@Preview
fun App() {
    AppTheme {
        // Minimal navigation: which screen we are on. Can move to navigation-compose later.
        var current by remember { mutableStateOf<Screen>(Screen.Splash) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (val screen = current) {
                Screen.Splash -> SplashScreen(
                    onFinished = { current = Screen.Welcome },
                )
                Screen.Welcome -> WelcomeScreen(
                    modifier = Modifier.safeContentPadding(),
                    onContinueAsGuest = { current = Screen.Home },
                    onGoogleSignIn = { /* TODO: Google sign-in later */ },
                )
                Screen.Home -> HomeScreen(
                    onGameSelected = { mode ->
                        when (mode) {
                            GameMode.X01 -> current = Screen.X01
                            GameMode.CRICKET, GameMode.TRAINING -> { /* TODO: Cricket / Training */ }
                        }
                    },
                    onHistory = { /* TODO: history */ },
                )
                Screen.X01 -> X01ScoreScreen(
                    onScoreSelected = { score -> current = Screen.Game(score) },
                    onBack = { current = Screen.Home },
                )
                is Screen.Game -> GameScreen(
                    startingScore = screen.startingScore,
                    onExit = { current = Screen.X01 },
                )
            }
        }
    }
}
