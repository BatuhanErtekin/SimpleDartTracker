package com.batu.simpledarttracker.ui.navigation

/**
 * The screens of the app. Currently driven by a single piece of state in App.kt;
 * this can move to navigation-compose as the app grows.
 */
sealed interface Screen {
    data object Splash : Screen
    data object Welcome : Screen
    data object Home : Screen
    data object X01 : Screen
    data class Game(val startingScore: Int) : Screen
}
