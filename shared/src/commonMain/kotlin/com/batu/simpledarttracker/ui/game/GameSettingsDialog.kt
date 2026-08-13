package com.batu.simpledarttracker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.ui.common.BackIcon
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_close
import simpledarttracker.shared.generated.resources.cd_back
import simpledarttracker.shared.generated.resources.cd_move_down
import simpledarttracker.shared.generated.resources.cd_move_up
import simpledarttracker.shared.generated.resources.settings_change_player_order
import simpledarttracker.shared.generated.resources.settings_reorder_hint
import simpledarttracker.shared.generated.resources.settings_title

private val RowHeight = 52.dp
private val RowSpacing = 8.dp

/**
 * In-match settings. Opens on a menu of options; picking one swaps the dialog over to that
 * option's page. Only the throwing order is adjustable so far.
 */
@Composable
fun GameSettingsDialog(
    players: List<Player>,
    currentIndex: Int,
    onMovePlayer: (fromIndex: Int, toIndex: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var onOrderPage by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Brand.Slate2,
        titleContentColor = Brand.Chalk,
        textContentColor = Brand.Chalk,
        title = {
            Text(
                stringResource(
                    if (onOrderPage) Res.string.settings_change_player_order else Res.string.settings_title,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Brand.Spruce),
            ) {
                Text(stringResource(Res.string.action_close))
            }
        },
        dismissButton = if (!onOrderPage) {
            null
        } else {
            {
                TextButton(
                    onClick = { onOrderPage = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Brand.Chalk),
                ) {
                    Text(stringResource(Res.string.cd_back))
                }
            }
        },
        text = {
            if (onOrderPage) {
                PlayerOrderPage(
                    players = players,
                    currentIndex = currentIndex,
                    onMovePlayer = onMovePlayer,
                )
            } else {
                SettingsMenu(onChangeOrder = { onOrderPage = true })
            }
        },
    )
}

@Composable
private fun SettingsMenu(onChangeOrder: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(RowSpacing)) {
        MenuRow(
            label = stringResource(Res.string.settings_change_player_order),
            onClick = onChangeOrder,
        )
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(Brand.Key)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Brand.Chalk,
            modifier = Modifier.weight(1f),
        )
        // The back chevron mirrored, so it points into the page it opens.
        BackIcon(
            color = Brand.Wire,
            contentDescription = null,
            modifier = Modifier.size(18.dp).rotate(180f),
        )
    }
}

/**
 * The reorder list. A player can be dragged after a long press, or nudged with the arrows —
 * both routes go through the same engine call, so the throw never changes hands.
 */
@Composable
private fun PlayerOrderPage(
    players: List<Player>,
    currentIndex: Int,
    onMovePlayer: (Int, Int) -> Unit,
) {
    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val stepPx = with(LocalDensity.current) { (RowHeight + RowSpacing).toPx() }
    val lastIndex = players.lastIndex

    Column(verticalArrangement = Arrangement.spacedBy(RowSpacing)) {
        Text(
            text = stringResource(Res.string.settings_reorder_hint),
            style = MaterialTheme.typography.bodySmall,
            color = Brand.Wire,
        )
        Column(
            modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(RowSpacing),
        ) {
            players.forEachIndexed { index, player ->
                // Keyed so a row keeps its identity — and its in-flight drag gesture — when
                // the list reorders underneath it.
                key(player.id) {
                    val liveIndex by rememberUpdatedState(index)
                    val isDragged = draggedId == player.id
                    PlayerOrderRow(
                        position = index + 1,
                        name = player.name,
                        isThrowing = index == currentIndex,
                        isDragged = isDragged,
                        canMoveUp = index > 0,
                        canMoveDown = index < lastIndex,
                        onMoveUp = { onMovePlayer(index, index - 1) },
                        onMoveDown = { onMovePlayer(index, index + 1) },
                        modifier = Modifier
                            .zIndex(if (isDragged) 1f else 0f)
                            .graphicsLayer { translationY = if (isDragged) dragOffset else 0f }
                            .pointerInput(player.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggedId = player.id; dragOffset = 0f },
                                    onDragEnd = { draggedId = null; dragOffset = 0f },
                                    onDragCancel = { draggedId = null; dragOffset = 0f },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.y
                                        val from = liveIndex
                                        // Swap once the row has travelled past half of its
                                        // neighbour, then carry the leftover offset so the row
                                        // stays under the finger.
                                        val to = when {
                                            dragOffset > stepPx / 2 && from < lastIndex -> from + 1
                                            dragOffset < -stepPx / 2 && from > 0 -> from - 1
                                            else -> null
                                        }
                                        if (to != null) {
                                            onMovePlayer(from, to)
                                            dragOffset -= (to - from) * stepPx
                                        }
                                    },
                                )
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerOrderRow(
    position: Int,
    name: String,
    isThrowing: Boolean,
    isDragged: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isDragged -> Brand.Spruce.copy(alpha = 0.85f)
                    isThrowing -> Brand.Spruce
                    else -> Brand.Key
                },
            )
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$position.",
            style = MaterialTheme.typography.labelLarge,
            color = Brand.Wire,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isThrowing) FontWeight.Bold else FontWeight.Normal,
            color = Brand.Chalk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        MoveButton(
            enabled = canMoveUp,
            rotation = 90f,
            contentDescription = stringResource(Res.string.cd_move_up),
            onClick = onMoveUp,
        )
        MoveButton(
            enabled = canMoveDown,
            rotation = -90f,
            contentDescription = stringResource(Res.string.cd_move_down),
            onClick = onMoveDown,
        )
    }
}

/** The back chevron turned a quarter turn, so it points up or down. */
@Composable
private fun MoveButton(
    enabled: Boolean,
    rotation: Float,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(36.dp)) {
        BackIcon(
            color = if (enabled) Brand.Chalk else Brand.Chalk.copy(alpha = 0.3f),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp).rotate(rotation),
        )
    }
}
