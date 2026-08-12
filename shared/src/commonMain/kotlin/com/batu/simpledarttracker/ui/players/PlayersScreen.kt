package com.batu.simpledarttracker.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.ui.common.BackIcon
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_start
import simpledarttracker.shared.generated.resources.cd_back
import simpledarttracker.shared.generated.resources.cd_decrease
import simpledarttracker.shared.generated.resources.cd_increase
import simpledarttracker.shared.generated.resources.player_word
import simpledarttracker.shared.generated.resources.players_count
import simpledarttracker.shared.generated.resources.players_title

private const val MIN_PLAYERS = 1
private const val MAX_PLAYERS = 8
private const val DEFAULT_PLAYERS = 2

/**
 * Player setup: first how many, then a name for each. Name fields start filled with
 * "Player N" and clear on first focus so typing is immediate; a field left blank falls back
 * to "Player N". [onStart] receives the resulting player list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    onStart: (List<Player>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerWord = stringResource(Res.string.player_word)
    fun defaultName(index: Int) = "$playerWord ${index + 1}"

    var count by remember { mutableIntStateOf(DEFAULT_PLAYERS) }
    val names = remember {
        mutableStateListOf<String>().apply {
            for (i in 0 until DEFAULT_PLAYERS) add("$playerWord ${i + 1}")
        }
    }

    val decreaseDesc = stringResource(Res.string.cd_decrease)
    val increaseDesc = stringResource(Res.string.cd_increase)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.players_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        BackIcon(
                            color = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(Res.string.cd_back),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val players = (0 until count).map { i ->
                        val name = names[i].ifBlank { defaultName(i) }
                        Player(id = "p${i + 1}", name = name)
                    }
                    onStart(players)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(16.dp),
            ) {
                Text(stringResource(Res.string.action_start))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(Res.string.players_count), style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedIconButton(
                        onClick = { if (count > MIN_PLAYERS) { count--; names.removeAt(names.lastIndex) } },
                        enabled = count > MIN_PLAYERS,
                        modifier = Modifier.semantics { contentDescription = decreaseDesc },
                    ) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.widthIn(min = 40.dp),
                        textAlign = TextAlign.Center,
                    )
                    OutlinedIconButton(
                        onClick = { if (count < MAX_PLAYERS) { count++; names.add("$playerWord $count") } },
                        enabled = count < MAX_PLAYERS,
                        modifier = Modifier.semantics { contentDescription = increaseDesc },
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            for (i in 0 until count) {
                PlayerNameField(
                    value = names[i],
                    default = defaultName(i),
                    onValueChange = { names[i] = it },
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlayerNameField(
    value: String,
    default: String,
    onValueChange: (String) -> Unit,
) {
    var cleared by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = { cleared = true; onValueChange(it) },
        placeholder = { Text(default) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !cleared && value == default) {
                    cleared = true
                    onValueChange("")
                }
            },
    )
}
