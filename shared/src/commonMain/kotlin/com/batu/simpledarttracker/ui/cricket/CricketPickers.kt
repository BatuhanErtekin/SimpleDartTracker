package com.batu.simpledarttracker.ui.cricket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.cricket.CricketTarget
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_cancel
import simpledarttracker.shared.generated.resources.action_confirm
import simpledarttracker.shared.generated.resources.cricket_house_hint
import simpledarttracker.shared.generated.resources.cricket_pick_number
import simpledarttracker.shared.generated.resources.label_bull
import simpledarttracker.shared.generated.resources.label_house

private const val NUMBERS_PER_ROW = 5

/**
 * Asks which number a double or treble was on.
 *
 * The ring targets take one mark whatever the number, but the points do not: a treble 20 hands
 * over sixty and a treble 3 only nine, which matters most when points are a punishment.
 */
@Composable
fun RingPickerDialog(
    target: CricketTarget,
    onPicked: (Dart) -> Unit,
    onDismiss: () -> Unit,
) {
    val ring = if (target == CricketTarget.AnyTriple) Ring.TRIPLE else Ring.DOUBLE
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Brand.Slate2,
        titleContentColor = Brand.Chalk,
        textContentColor = Brand.Chalk,
        title = { Text(stringResource(Res.string.cricket_pick_number)) },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Chalk),
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NumberGrid(onPick = { n -> onPicked(Dart.Segment(n, ring)) })
                // The inner bull is a double, so it belongs on the double picker only.
                if (ring == Ring.DOUBLE) {
                    PickerChip(
                        label = stringResource(Res.string.label_bull),
                        selected = false,
                        onClick = { onPicked(Dart.DoubleBull) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    )
}

/**
 * Collects a house: the target it was on, and how each of the three darts landed.
 *
 * A house is only known once the turn is over and its penalty is the three darts added up, so
 * the whole turn is described here in one go rather than dart by dart.
 */
@Composable
fun HousePickerDialog(
    onPicked: (List<Dart>) -> Unit,
    onDismiss: () -> Unit,
) {
    var number by remember { mutableStateOf<Int?>(null) }
    var onBull by remember { mutableStateOf(false) }
    var rings by remember { mutableStateOf(listOf(0, 0, 0)) }

    val choices: List<Pair<String, (Int) -> Dart>> = if (onBull) {
        listOf("25" to { _: Int -> Dart.Bull }, "50" to { _: Int -> Dart.DoubleBull })
    } else {
        listOf(
            "S" to { n: Int -> Dart.Segment(n, Ring.SINGLE) },
            "D" to { n: Int -> Dart.Segment(n, Ring.DOUBLE) },
            "T" to { n: Int -> Dart.Segment(n, Ring.TRIPLE) },
        )
    }
    val chosen = onBull || number != null
    val darts = if (!chosen) emptyList() else rings.map { choices[it].second(number ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Brand.Slate2,
        titleContentColor = Brand.Chalk,
        textContentColor = Brand.Chalk,
        title = { Text(stringResource(Res.string.label_house)) },
        confirmButton = {
            TextButton(
                onClick = { onPicked(darts) },
                enabled = chosen,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Spruce),
            ) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Chalk),
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.cricket_house_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Brand.Wire,
                )
                NumberGrid(
                    selected = number.takeIf { !onBull },
                    onPick = { n -> number = n; onBull = false; rings = listOf(0, 0, 0) },
                )
                PickerChip(
                    label = stringResource(Res.string.label_bull),
                    selected = onBull,
                    onClick = { onBull = true; number = null; rings = listOf(0, 0, 0) },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (chosen) {
                    rings.forEachIndexed { dartIndex, choice ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${dartIndex + 1}.",
                                style = MaterialTheme.typography.labelLarge,
                                color = Brand.Wire,
                            )
                            choices.forEachIndexed { optionIndex, (label, _) ->
                                PickerChip(
                                    label = label,
                                    selected = optionIndex == choice,
                                    onClick = {
                                        rings = rings.toMutableList()
                                            .also { it[dartIndex] = optionIndex }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun NumberGrid(
    selected: Int? = null,
    onPick: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        (20 downTo 1).chunked(NUMBERS_PER_ROW).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { n ->
                    PickerChip(
                        label = n.toString(),
                        selected = n == selected,
                        onClick = { onPick(n) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Brand.Spruce else Brand.Key)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = Brand.Chalk,
        )
    }
}
