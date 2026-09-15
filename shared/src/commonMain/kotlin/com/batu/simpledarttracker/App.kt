package com.batu.simpledarttracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batu.simpledarttracker.domain.game.GameMode
import com.batu.simpledarttracker.ui.cricket.CricketBoard
import com.batu.simpledarttracker.ui.cricket.CricketViewModel
import com.batu.simpledarttracker.ui.game.LeaveGameDialog
import com.batu.simpledarttracker.ui.home.HomeScreen
import com.batu.simpledarttracker.ui.navigation.NavigationViewModel
import com.batu.simpledarttracker.ui.navigation.PlatformBackHandler
import com.batu.simpledarttracker.ui.platform.KeepScreenAwake
import com.batu.simpledarttracker.ui.navigation.Screen
import com.batu.simpledarttracker.ui.setup.GameSetupScreen
import com.batu.simpledarttracker.ui.setup.MatchSetup
import com.batu.simpledarttracker.ui.splash.SplashScreen
import com.batu.simpledarttracker.ui.theme.AppTheme
import com.batu.simpledarttracker.ui.theme.appBackdrop
import com.batu.simpledarttracker.ui.welcome.WelcomeScreen
import com.batu.simpledarttracker.ui.x01.X01Board
import com.batu.simpledarttracker.ui.x01.X01ViewModel

/**
 * The app shell. It owns nothing but the wiring: which screen the back stack points at, and
 * which view model each screen talks to. All state lives in the view models, so a rotation
 * does not lose the match in progress.
 */
@Composable
@Preview
fun App() {
    AppTheme {
        // The phone sits on the table while you throw; it should not lock itself between legs.
        KeepScreenAwake()
        val navigation: NavigationViewModel = viewModel { NavigationViewModel() }
        val x01: X01ViewModel = viewModel { X01ViewModel() }
        val cricket: CricketViewModel = viewModel { CricketViewModel() }
        val backStack by navigation.backStack.collectAsState()
        var confirmLeave by rememberSaveable { mutableStateOf(false) }

        // Walk our own stack instead of closing the app. At the root there is nothing to pop,
        // so the handler switches off and the system takes the app to the background.
        // Backing out of a match asks first, since the game is not stored anywhere.
        PlatformBackHandler(enabled = backStack.size > 1) {
            if (backStack.last() is Screen.Match) confirmLeave = true else navigation.back()
        }

        if (confirmLeave) {
            LeaveGameDialog(
                onLeave = {
                    confirmLeave = false
                    x01.end()
                    cricket.end()
                    navigation.back()
                },
                onCancel = { confirmLeave = false },
            )
        }

        // The page is painted rather than filled: see Backdrop.kt. The surface on top of it
        // carries the content colour and nothing else.
        Surface(
            modifier = Modifier.fillMaxSize().appBackdrop(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            when (val screen = backStack.last()) {
                Screen.Splash -> SplashScreen(
                    onFinished = { navigation.reset(Screen.Welcome) },
                )

                Screen.Welcome -> WelcomeScreen(
                    modifier = Modifier.safeContentPadding(),
                    onContinueAsGuest = { navigation.reset(Screen.Home) },
                    onGoogleSignIn = { /* TODO: Google sign-in later */ },
                )

                Screen.Home -> HomeScreen(
                    onGameSelected = { mode -> navigation.push(Screen.Setup(mode)) },
                )

                is Screen.Setup -> GameSetupScreen(
                    mode = screen.mode,
                    onBack = navigation::back,
                    onStart = { setup ->
                        when (setup) {
                            is MatchSetup.X01 -> {
                                x01.start(setup.config, setup.players)
                                navigation.push(Screen.Match(GameMode.X01))
                            }
                            is MatchSetup.Cricket -> {
                                cricket.start(setup.config, setup.players)
                                navigation.push(Screen.Match(GameMode.CRICKET))
                            }
                        }
                    },
                )

                is Screen.Match -> when (screen.mode) {
                    GameMode.X01 -> X01Match(x01, navigation)
                    GameMode.CRICKET -> CricketMatch(cricket, navigation)
                    GameMode.TRAINING -> Unit
                }
            }
        }
    }
}

@Composable
private fun X01Match(viewModel: X01ViewModel, navigation: NavigationViewModel) {
    val game by viewModel.game.collectAsState()
    val lastTurn by viewModel.lastTurn.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val state = game
    if (state == null) {
        // Nothing to show. This only happens if the match was cleared while the board was on
        // screen, so step back rather than render an empty arena.
        LaunchedEffect(Unit) { navigation.back() }
        return
    }
    X01Board(
        state = state,
        lastTurn = lastTurn,
        canUndo = canUndo,
        onDart = viewModel::throwDart,
        onUndo = viewModel::undo,
        onPlayAgain = viewModel::playAgain,
        onMovePlayer = viewModel::movePlayer,
        onHome = { viewModel.end(); navigation.reset(Screen.Home) },
    )
}

@Composable
private fun CricketMatch(viewModel: CricketViewModel, navigation: NavigationViewModel) {
    val game by viewModel.game.collectAsState()
    val lastTurn by viewModel.lastTurn.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val state = game ?: run {
        LaunchedEffect(Unit) { navigation.back() }
        return
    }
    CricketBoard(
        state = state,
        lastTurn = lastTurn,
        canUndo = canUndo,
        onThrow = viewModel::throwFor,
        onClaimHouse = viewModel::claimHouseFor,
        onUndo = viewModel::undo,
        onPlayAgain = viewModel::playAgain,
        onMovePlayer = viewModel::movePlayer,
        onHome = { viewModel.end(); navigation.reset(Screen.Home) },
    )
}
