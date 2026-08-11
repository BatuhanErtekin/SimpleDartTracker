package com.batu.simpledarttracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.model.GameMode
import com.batu.simpledarttracker.ui.brand.DartMark
import com.batu.simpledarttracker.ui.common.HamburgerIcon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.app_name
import simpledarttracker.shared.generated.resources.cd_open_settings
import simpledarttracker.shared.generated.resources.home_history
import simpledarttracker.shared.generated.resources.home_history_subtitle
import simpledarttracker.shared.generated.resources.mode_cricket
import simpledarttracker.shared.generated.resources.mode_cricket_subtitle
import simpledarttracker.shared.generated.resources.mode_training
import simpledarttracker.shared.generated.resources.mode_training_subtitle
import simpledarttracker.shared.generated.resources.mode_x01
import simpledarttracker.shared.generated.resources.mode_x01_subtitle
import simpledarttracker.shared.generated.resources.settings_default_rules
import simpledarttracker.shared.generated.resources.settings_theme
import simpledarttracker.shared.generated.resources.settings_title

/** A single entry in the main menu. */
private data class HomeAction(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

/**
 * The main menu shown after continuing without an account. Game modes are listed directly;
 * the app name and icon sit at the top, and the hamburger on the left (or a swipe from the
 * left edge) opens the settings drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameSelected: (GameMode) -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val actions = listOf(
        HomeAction("🎯", stringResource(Res.string.mode_x01), stringResource(Res.string.mode_x01_subtitle)) { onGameSelected(GameMode.X01) },
        HomeAction("🦗", stringResource(Res.string.mode_cricket), stringResource(Res.string.mode_cricket_subtitle)) { onGameSelected(GameMode.CRICKET) },
        HomeAction("🔁", stringResource(Res.string.mode_training), stringResource(Res.string.mode_training_subtitle)) { onGameSelected(GameMode.TRAINING) },
        HomeAction("📊", stringResource(Res.string.home_history), stringResource(Res.string.home_history_subtitle), onHistory),
    )

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = { SettingsDrawer() },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DartMark(
                                lineColor = MaterialTheme.colorScheme.onSurface,
                                greenColor = MaterialTheme.colorScheme.primary,
                                redColor = MaterialTheme.colorScheme.secondary,
                                haloColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(28.dp),
                            )
                            Text(
                                text = stringResource(Res.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            HamburgerIcon(
                                color = MaterialTheme.colorScheme.onSurface,
                                contentDescription = stringResource(Res.string.cd_open_settings),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(actions) { action ->
                    HomeActionCard(action)
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(action: HomeAction) {
    Card(
        onClick = action.onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(action.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Settings drawer — placeholder rows for now. */
@Composable
private fun SettingsDrawer() {
    ModalDrawerSheet {
        Text(
            text = stringResource(Res.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(24.dp),
        )
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.settings_theme)) },
            selected = false,
            onClick = { /* TODO: theme picker */ },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text(stringResource(Res.string.settings_default_rules)) },
            selected = false,
            onClick = { /* TODO: default rules */ },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}
