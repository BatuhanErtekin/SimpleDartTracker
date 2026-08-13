package com.batu.simpledarttracker.ui.navigation

import com.batu.simpledarttracker.domain.game.GameMode

/**
 * The screens of the app. Match settings are no longer carried here: the setup screen collects
 * them and hands them to the game's view model, so navigation only needs to know which mode
 * is being played.
 */
sealed interface Screen {
    data object Splash : Screen
    data object Welcome : Screen
    data object Home : Screen
    data class Setup(val mode: GameMode) : Screen
    data class Match(val mode: GameMode) : Screen
}
