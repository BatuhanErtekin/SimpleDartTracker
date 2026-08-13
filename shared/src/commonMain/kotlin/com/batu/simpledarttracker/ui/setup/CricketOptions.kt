package com.batu.simpledarttracker.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.cricket.CricketScoring
import com.batu.simpledarttracker.domain.cricket.CricketTarget
import com.batu.simpledarttracker.domain.cricket.MarkChoices
import com.batu.simpledarttracker.domain.cricket.marksToClose
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cricket_scoring
import simpledarttracker.shared.generated.resources.cricket_scoring_penalty
import simpledarttracker.shared.generated.resources.cricket_scoring_penalty_hint
import simpledarttracker.shared.generated.resources.cricket_scoring_plain
import simpledarttracker.shared.generated.resources.cricket_scoring_plain_hint
import simpledarttracker.shared.generated.resources.label_bull
import simpledarttracker.shared.generated.resources.label_double
import simpledarttracker.shared.generated.resources.label_house
import simpledarttracker.shared.generated.resources.label_triple
import simpledarttracker.shared.generated.resources.preset_custom
import simpledarttracker.shared.generated.resources.preset_random
import simpledarttracker.shared.generated.resources.preset_standard
import simpledarttracker.shared.generated.resources.rules_targets

/** Which target preset is active. Stored as an index so it survives a rotation. */
const val CRICKET_PRESET_STANDARD = 0
const val CRICKET_PRESET_RANDOM = 1
const val CRICKET_PRESET_CUSTOM = 2

private const val NUMBERS_PER_ROW = 5

/**
 * The Cricket-specific part of the setup screen: which targets are in play and whether points
 * reward or punish.
 *
 * Standard and Random fill the selection in; touching any target or mark count afterwards drops
 * the choice to Custom. Tapping Random again draws a fresh set, and Custom can also be picked
 * outright to keep the current selection while editing it freely.
 */
@Composable
fun CricketOptions(
    preset: Int,
    selectedTargets: Set<CricketTarget>,
    marks: Map<CricketTarget, Int>,
    onStandard: () -> Unit,
    onRandom: () -> Unit,
    onCustom: () -> Unit,
    onToggleTarget: (CricketTarget) -> Unit,
    onMarksChange: (CricketTarget, Int) -> Unit,
    scoring: CricketScoring,
    onScoringChange: (CricketScoring) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PresetTile(
                text = stringResource(Res.string.preset_standard),
                selected = preset == CRICKET_PRESET_STANDARD,
                onClick = onStandard,
                modifier = Modifier.weight(1f),
            )
            PresetTile(
                text = stringResource(Res.string.preset_random),
                selected = preset == CRICKET_PRESET_RANDOM,
                onClick = onRandom,
                modifier = Modifier.weight(1f),
            )
            PresetTile(
                text = stringResource(Res.string.preset_custom),
                selected = preset == CRICKET_PRESET_CUSTOM,
                onClick = onCustom,
                modifier = Modifier.weight(1f),
            )
        }

        SectionLabel(stringResource(Res.string.rules_targets))
        CricketTarget.numbers.chunked(NUMBERS_PER_ROW).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { target ->
                    TargetChip(
                        label = target.value.toString(),
                        selected = target in selectedTargets,
                        onClick = { onToggleTarget(target) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        // The extras carry their own mark requirement, so each sits above its own ×1/×2/×3.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CricketTarget.extras.forEach { target ->
                val isSelected = target in selectedTargets
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TargetChip(
                        label = extraLabel(target),
                        selected = isSelected,
                        onClick = { onToggleTarget(target) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    MarkSelector(
                        value = marksToClose(target, marks),
                        dimmed = !isSelected,
                        onSelect = { onMarksChange(target, it) },
                    )
                }
            }
        }

        SectionLabel(stringResource(Res.string.cricket_scoring))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PresetTile(
                text = stringResource(Res.string.cricket_scoring_plain),
                selected = scoring == CricketScoring.STANDARD,
                onClick = { onScoringChange(CricketScoring.STANDARD) },
                modifier = Modifier.weight(1f),
            )
            PresetTile(
                text = stringResource(Res.string.cricket_scoring_penalty),
                selected = scoring == CricketScoring.CUT_THROAT,
                onClick = { onScoringChange(CricketScoring.CUT_THROAT) },
                modifier = Modifier.weight(1f),
            )
        }
        // Cut-throat is the rule people get wrong, so spell out whichever is selected.
        Text(
            text = stringResource(
                when (scoring) {
                    CricketScoring.STANDARD -> Res.string.cricket_scoring_plain_hint
                    CricketScoring.CUT_THROAT -> Res.string.cricket_scoring_penalty_hint
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun extraLabel(target: CricketTarget): String = when (target) {
    CricketTarget.Bull -> stringResource(Res.string.label_bull)
    CricketTarget.AnyDouble -> stringResource(Res.string.label_double)
    CricketTarget.AnyTriple -> stringResource(Res.string.label_triple)
    CricketTarget.House -> stringResource(Res.string.label_house)
    is CricketTarget.Number -> target.value.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetTile(
    text: String,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = setupTileColors(selected)
    val label = @Composable {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 4.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
    if (onClick == null) {
        Card(modifier = modifier, colors = colors) { label() }
    } else {
        Card(onClick = onClick, modifier = modifier, colors = colors) { label() }
    }
}

/**
 * The ×1/×2/×3 strip under an extra target. It stays tappable while the target is unselected —
 * picking a count selects the target too — but dims so it reads as not yet in play.
 */
@Composable
private fun MarkSelector(
    value: Int,
    dimmed: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().graphicsLayer { alpha = if (dimmed) 0.45f else 1f },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MarkChoices.forEach { choice ->
            val active = !dimmed && choice == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    )
                    .clickable { onSelect(choice) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×$choice",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.height(48.dp), colors = setupTileColors(selected)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}
