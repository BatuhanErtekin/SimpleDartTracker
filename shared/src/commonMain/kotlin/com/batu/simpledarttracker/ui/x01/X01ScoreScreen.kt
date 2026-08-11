package com.batu.simpledarttracker.ui.x01

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batu.simpledarttracker.ui.common.BackIcon
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cd_back
import simpledarttracker.shared.generated.resources.cd_decrease
import simpledarttracker.shared.generated.resources.cd_increase
import simpledarttracker.shared.generated.resources.preset_custom
import simpledarttracker.shared.generated.resources.rules_starting_score
import simpledarttracker.shared.generated.resources.x01_tap_to_start
import simpledarttracker.shared.generated.resources.x01_title

private const val CUSTOM_MIN = 101
private const val CUSTOM_MAX = 1001
private const val CUSTOM_STEP = 100
private const val CUSTOM_DEFAULT = 701

/**
 * X01 starting-score picker. 301 and 501 are large tiles; below them sits a custom score
 * whose leading digits change with the ± buttons or a vertical drag. Tapping a score calls
 * [onScoreSelected] with it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun X01ScoreScreen(
    onScoreSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.x01_title)) },
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
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionLabel(stringResource(Res.string.rules_starting_score))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PresetScoreTile(score = 301, modifier = Modifier.weight(1f)) { onScoreSelected(301) }
                PresetScoreTile(score = 501, modifier = Modifier.weight(1f)) { onScoreSelected(501) }
            }
            CustomScoreTile(onSelect = onScoreSelected)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PresetScoreTile(score: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayMedium,
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

@Composable
private fun CustomScoreTile(onSelect: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(CUSTOM_DEFAULT) }
    val onDelta: (Int) -> Unit = { d -> score = (score + d).coerceIn(CUSTOM_MIN, CUSTOM_MAX) }
    val stepPx = with(LocalDensity.current) { 44.dp.toPx() }
    val decreaseDesc = stringResource(Res.string.cd_decrease)
    val increaseDesc = stringResource(Res.string.cd_increase)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.preset_custom).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedIconButton(
                    onClick = { onDelta(-CUSTOM_STEP) },
                    enabled = score > CUSTOM_MIN,
                    modifier = Modifier.semantics { contentDescription = decreaseDesc },
                ) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }

                // The big score in the middle: tap to pick it, drag vertically to change it.
                Row(
                    modifier = Modifier
                        .clickable { onSelect(score) }
                        .pointerInput(Unit) {
                            var acc = 0f
                            detectVerticalDragGestures { _, dragAmount ->
                                acc += dragAmount
                                while (acc <= -stepPx) { onDelta(CUSTOM_STEP); acc += stepPx }
                                while (acc >= stepPx) { onDelta(-CUSTOM_STEP); acc -= stepPx }
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = (score / 100).toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "01",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                OutlinedIconButton(
                    onClick = { onDelta(CUSTOM_STEP) },
                    enabled = score < CUSTOM_MAX,
                    modifier = Modifier.semantics { contentDescription = increaseDesc },
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(
                text = stringResource(Res.string.x01_tap_to_start),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
