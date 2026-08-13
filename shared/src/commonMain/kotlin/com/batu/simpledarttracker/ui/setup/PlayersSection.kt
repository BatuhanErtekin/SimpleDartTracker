package com.batu.simpledarttracker.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.ui.common.Stepper
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.players_count

const val MIN_PLAYERS = 1
const val MAX_PLAYERS = 8

/**
 * Player count and names. Name fields start filled with "Player N" and clear on first focus so
 * typing is immediate; a field left blank falls back to "Player N" when the match starts.
 */
@Composable
fun PlayersSection(
    names: List<String>,
    onNamesChange: (List<String>) -> Unit,
    defaultName: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.players_count), style = MaterialTheme.typography.titleMedium)
            Stepper(
                value = names.size,
                onValueChange = { count ->
                    onNamesChange(List(count) { i -> names.getOrNull(i) ?: defaultName(i) })
                },
                range = MIN_PLAYERS..MAX_PLAYERS,
            )
        }

        names.forEachIndexed { index, name ->
            PlayerNameField(
                value = name,
                default = defaultName(index),
                onValueChange = { updated ->
                    onNamesChange(names.toMutableList().also { it[index] = updated })
                },
            )
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
