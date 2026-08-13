package com.batu.simpledarttracker.ui.game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.ui.theme.Brand

// A key splits its width by weight: the label takes half, the D/T sections a quarter each.
// A key with a single option gives it the room of both, so that key reads 50/50.
private const val KEY_LABEL_WEIGHT = 2f
const val KEY_OPTION_WEIGHT = 1f
const val KEY_WIDE_OPTION_WEIGHT = 2f

/** A section attached to the side of a key, such as the double and treble of a number. */
data class KeyOption<T>(
    val letter: String,
    val color: Color,
    val value: T,
    val menuLabel: String,
    val weight: Float = KEY_OPTION_WEIGHT,
)

/** One key of the pad: tapping the label emits [value], the sections emit their own. */
data class DartKeySpec<T>(
    val label: String,
    val value: T,
    val options: List<KeyOption<T>> = emptyList(),
)

/**
 * The entry pad, laid out two keys per row in the order given. Callers decide which keys exist
 * and in what order, so a game that only uses part of the board can supply just those.
 *
 * What a key emits is up to the caller too: X01 keys emit the dart itself, while Cricket keys
 * emit an action, because some of its keys open a picker instead of scoring straight away.
 */
@Composable
fun <T> DartPad(
    keys: List<DartKeySpec<T>>,
    enabled: Boolean,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        keys.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { key ->
                    DartKey(
                        spec = key,
                        enabled = enabled,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> DartKey(
    spec: DartKeySpec<T>,
    enabled: Boolean,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier.clip(RoundedCornerShape(12.dp))) {
        // Single (tap) plus the long-press menu.
        Box(
            modifier = Modifier
                .weight(KEY_LABEL_WEIGHT)
                .fillMaxHeight()
                .background(if (enabled) Brand.Key else Brand.Key.copy(alpha = 0.4f))
                .combinedClickable(
                    enabled = enabled,
                    onClick = { onSelect(spec.value) },
                    onLongClick = { if (spec.options.isNotEmpty()) expanded = true },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = spec.label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Brand.Chalk else Brand.Wire,
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                spec.options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.menuLabel) },
                        onClick = { onSelect(opt.value); expanded = false },
                    )
                }
            }
        }
        // Adjacent D / T sections.
        spec.options.forEach { opt ->
            Box(Modifier.width(1.5.dp).fillMaxHeight().background(Brand.Slate))
            Box(
                modifier = Modifier
                    .weight(opt.weight)
                    .fillMaxHeight()
                    .background(if (enabled) opt.color else opt.color.copy(alpha = 0.35f))
                    .clickable(enabled = enabled) { onSelect(opt.value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = opt.letter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Brand.Ink,
                )
            }
        }
    }
}

/** Short label for a thrown dart, as shown in the turn's dart slots. */
fun dartLabel(dart: Dart): String = when (dart) {
    is Dart.Segment -> when (dart.ring) {
        Ring.SINGLE -> dart.number.toString()
        Ring.DOUBLE -> "D${dart.number}"
        Ring.TRIPLE -> "T${dart.number}"
    }
    Dart.Bull -> "25"
    Dart.DoubleBull -> "50"
    Dart.Miss -> "0"
}
