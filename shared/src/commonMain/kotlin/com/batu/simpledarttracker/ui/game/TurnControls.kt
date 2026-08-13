package com.batu.simpledarttracker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.ui.common.bottomBarInsets
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_undo
import simpledarttracker.shared.generated.resources.label_miss

/** A badge shown next to the dart slots when a turn resolves early. */
data class OutcomeBadge(val text: String, val color: Color)

/**
 * The panel under the board: the three slots of the turn on show, an optional outcome badge,
 * and undo. Identical for every game, so both the badge and how each slot reads are supplied by
 * the caller — Cricket writes a slot by the target it was claimed for, which is not always the
 * same as the dart.
 *
 * There is no confirm: a turn lands as soon as it resolves. When one has just landed the slots
 * go on showing it, so a bust can still be read after the throw has passed on.
 *
 * [onMiss] is for boards with no key of their own for a dart that hit nothing — without it a
 * turn with fewer than three scoring darts could never reach three, and so would never land.
 */
@Composable
fun TurnControls(
    slots: List<String>,
    badge: OutcomeBadge?,
    canUndo: Boolean,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
    onMiss: (() -> Unit)? = null,
) {
    Surface(modifier = modifier, color = Brand.Slate2, contentColor = Brand.Chalk) {
        Column(
            // The surface itself runs to the bottom edge; only its content clears the
            // navigation bar, so the bar sits on the panel colour rather than on the buttons.
            modifier = Modifier.fillMaxWidth().bottomBarInsets().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { i ->
                    DartSlot(label = slots.getOrNull(i), modifier = Modifier.weight(1f))
                }
                if (badge != null) {
                    Text(
                        text = badge.text,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = badge.color,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand.Chalk),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.action_undo))
                }
                if (onMiss != null) {
                    Button(
                        onClick = onMiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand.Key,
                            contentColor = Brand.Chalk,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.label_miss))
                    }
                }
            }
        }
    }
}

@Composable
private fun DartSlot(label: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brand.Key),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label ?: "–",
            style = MaterialTheme.typography.titleMedium,
            color = if (label == null) Brand.Wire else Brand.Chalk,
            textAlign = TextAlign.Center,
        )
    }
}
