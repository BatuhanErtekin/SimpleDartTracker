package com.batu.simpledarttracker.ui.setup

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batu.simpledarttracker.ui.common.Stepper
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.out_double
import simpledarttracker.shared.generated.resources.out_straight
import simpledarttracker.shared.generated.resources.preset_custom
import simpledarttracker.shared.generated.resources.rules_out_mode
import simpledarttracker.shared.generated.resources.rules_starting_score

const val CUSTOM_MIN = 101
const val CUSTOM_MAX = 1001
const val CUSTOM_STEP = 100
const val CUSTOM_DEFAULT = 701

/** Which starting-score tile is selected. Stored as an index so it survives a rotation. */
const val PRESET_301 = 0
const val PRESET_501 = 1
const val PRESET_CUSTOM = 2

/** Resolves the selected tile and custom value into the score the match will start from. */
fun startingScoreOf(preset: Int, customScore: Int): Int = when (preset) {
    PRESET_301 -> 301
    PRESET_501 -> 501
    else -> customScore
}

/** The X01-specific part of the setup screen: starting score and finish rule. */
@Composable
fun X01Options(
    preset: Int,
    onPresetChange: (Int) -> Unit,
    customScore: Int,
    onCustomScoreChange: (Int) -> Unit,
    doubleOut: Boolean,
    onDoubleOutChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel(stringResource(Res.string.rules_starting_score))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PresetTile(
                score = 301,
                selected = preset == PRESET_301,
                onClick = { onPresetChange(PRESET_301) },
                modifier = Modifier.weight(1f),
            )
            PresetTile(
                score = 501,
                selected = preset == PRESET_501,
                onClick = { onPresetChange(PRESET_501) },
                modifier = Modifier.weight(1f),
            )
        }
        CustomTile(
            score = customScore,
            selected = preset == PRESET_CUSTOM,
            onSelect = { onPresetChange(PRESET_CUSTOM) },
            onScoreChange = { onCustomScoreChange(it); onPresetChange(PRESET_CUSTOM) },
        )

        SectionLabel(stringResource(Res.string.rules_out_mode))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChoiceTile(
                text = stringResource(Res.string.out_double),
                selected = doubleOut,
                onClick = { onDoubleOutChange(true) },
                modifier = Modifier.weight(1f),
            )
            ChoiceTile(
                text = stringResource(Res.string.out_straight),
                selected = !doubleOut,
                onClick = { onDoubleOutChange(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetTile(
    score: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.height(120.dp), colors = setupTileColors(selected)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "X01",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTile(
    score: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    onScoreChange: (Int) -> Unit,
) {
    Card(onClick = onSelect, modifier = Modifier.fillMaxWidth(), colors = setupTileColors(selected)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.preset_custom).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Stepper(
                value = score,
                onValueChange = onScoreChange,
                range = CUSTOM_MIN..CUSTOM_MAX,
                step = CUSTOM_STEP,
                valueStyle = MaterialTheme.typography.displaySmall,
                spacing = 20,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceTile(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier, colors = setupTileColors(selected)) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            textAlign = TextAlign.Center,
        )
    }
}
