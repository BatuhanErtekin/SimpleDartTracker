package com.batu.simpledarttracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.game.GameMode
import com.batu.simpledarttracker.ui.brand.DartMark
import com.batu.simpledarttracker.ui.common.HamburgerIcon
import com.batu.simpledarttracker.ui.theme.Brand
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.app_name
import simpledarttracker.shared.generated.resources.cd_open_settings
import simpledarttracker.shared.generated.resources.mode_cricket
import simpledarttracker.shared.generated.resources.mode_training
import simpledarttracker.shared.generated.resources.mode_x01
import simpledarttracker.shared.generated.resources.settings_default_rules
import simpledarttracker.shared.generated.resources.settings_theme
import simpledarttracker.shared.generated.resources.settings_title

private val WheelMaxWidth = 380.dp

/**
 * The main menu: one dartboard, cut into a slice per game. Everything below it is deliberately
 * empty for now — that is where match settings and the like will go.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sectors = listOf(
        WheelSector(GameMode.X01, stringResource(Res.string.mode_x01), Brand.Gold),
        WheelSector(GameMode.TRAINING, stringResource(Res.string.mode_training), Brand.Honey),
        WheelSector(GameMode.CRICKET, stringResource(Res.string.mode_cricket), Brand.Ember),
    )

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = { SettingsDrawer() },
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DartMark(modifier = Modifier.size(28.dp))
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                GameWheel(
                    sectors = sectors,
                    onSelect = onGameSelected,
                    modifier = Modifier
                        .widthIn(max = WheelMaxWidth)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
                // Room for what comes next; the board should not float in the middle.
                Spacer(Modifier.weight(1f))
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
