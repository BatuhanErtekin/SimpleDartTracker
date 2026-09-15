package com.batu.simpledarttracker.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cd_back

/**
 * The frame every screen shares: a title bar with an optional back button, and a bottom bar
 * that keeps its content clear of the navigation bar while its background still runs to the
 * screen edge.
 *
 * [containerColor] is transparent by default so the painted page (see Backdrop.kt) shows through
 * — a scaffold that fills with the theme background hides it completely. The game board overrides
 * it, because a board wants one flat colour under the numbers rather than a lit room. Screens that want something other than a back arrow in the top-left —
 * the board puts its settings button there — pass their own [navigationIcon].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    navigationIcon: @Composable () -> Unit = {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                BackIcon(
                    color = contentColor,
                    contentDescription = stringResource(Res.string.cd_back),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    },
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = contentColor,
                ),
            )
        },
        bottomBar = bottomBar,
        content = content,
    )
}

/** Padding that lifts a bottom bar's content above the navigation bar. */
@Composable
fun Modifier.bottomBarInsets(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
