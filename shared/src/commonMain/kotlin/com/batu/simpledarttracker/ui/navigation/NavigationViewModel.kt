package com.batu.simpledarttracker.ui.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the back stack. Keeping it in a view model rather than in `remember` means the app
 * stays where it was across a rotation, and gives every screen one way to go back instead of
 * the ad-hoc callbacks the screens used to pass around.
 */
class NavigationViewModel : ViewModel() {

    private val _backStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val backStack: StateFlow<List<Screen>> = _backStack.asStateFlow()

    fun push(screen: Screen) = _backStack.update { it + screen }

    /** Replaces the whole stack — for one-way steps such as splash → welcome. */
    fun reset(screen: Screen) {
        _backStack.value = listOf(screen)
    }

    /** Pops the top screen. The root screen is never popped. */
    fun back() = _backStack.update { if (it.size > 1) it.dropLast(1) else it }
}
