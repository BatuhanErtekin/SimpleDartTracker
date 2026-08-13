package com.batu.simpledarttracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cd_decrease
import simpledarttracker.shared.generated.resources.cd_increase

/**
 * A "− value +" control. Used for the player count and the custom starting score, which
 * previously carried two near-identical copies of this.
 */
@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 1..99,
    step: Int = 1,
    valueLabel: String = value.toString(),
    valueStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    spacing: Int = 12,
) {
    val decreaseDesc = stringResource(Res.string.cd_decrease)
    val increaseDesc = stringResource(Res.string.cd_increase)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedIconButton(
            onClick = { onValueChange((value - step).coerceIn(range)) },
            enabled = value > range.first,
            modifier = Modifier.semantics { contentDescription = decreaseDesc },
        ) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = valueLabel,
            style = valueStyle,
            modifier = Modifier.widthIn(min = 40.dp),
            textAlign = TextAlign.Center,
        )
        OutlinedIconButton(
            onClick = { onValueChange((value + step).coerceIn(range)) },
            enabled = value < range.last,
            modifier = Modifier.semantics { contentDescription = increaseDesc },
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
