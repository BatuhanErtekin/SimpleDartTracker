package com.batu.simpledarttracker.ui.cricket

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.cricket.CricketEngine
import com.batu.simpledarttracker.domain.cricket.CricketGameState
import com.batu.simpledarttracker.domain.cricket.CricketPlayerState
import com.batu.simpledarttracker.domain.cricket.CricketTarget
import com.batu.simpledarttracker.domain.cricket.CricketThrow
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.ui.game.dartLabel
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.label_bull
import simpledarttracker.shared.generated.resources.label_double
import simpledarttracker.shared.generated.resources.label_double_bull
import simpledarttracker.shared.generated.resources.label_house
import simpledarttracker.shared.generated.resources.label_triple

private val TargetColumnWidth = 64.dp
private val UnderlineWidth = 28.dp
private const val TURN_ANIMATION_MILLIS = 260
private val deadRule = Brand.Wire.copy(alpha = 0.45f)

/** A double or treble reachable by long-pressing a cell. */
private data class CellOption(val letter: String, val label: String, val dart: Dart)

/**
 * The scoreboard, which is also the way darts are entered — there is no separate pad, so the
 * whole screen is the board and every cell is big enough to hit.
 *
 * Tapping your own cell scores a single on that target; a long press offers the double and
 * treble. The ring and house targets ask for the detail only when it would change something —
 * once the mark starts spilling into points — so most of the game stays one tap.
 *
 * Any column takes input, not just the one whose turn it is: tapping another player's cell is
 * how the board is told the visit has moved on, which passes the throw and records the mark in
 * one go. It also means a short visit needs no "I missed" button to end it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CricketScoreboard(
    state: CricketGameState,
    enabled: Boolean,
    onThrow: (playerIndex: Int, CricketThrow) -> Unit,
    onPickRing: (playerIndex: Int, CricketTarget) -> Unit,
    onPickHouse: (playerIndex: Int) -> Unit,
    onClaimHouse: (playerIndex: Int, List<Dart>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Marks and scores are drawn from the projection so they keep up with the darts; the
    // questions the board asks are answered from the real state, which has yet to absorb them.
    val shown = CricketEngine.preview(state)
    val needsDetail: (Int, CricketTarget) -> Boolean = { player, target ->
        CricketEngine.wouldScore(state, target, player)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        ScoreHeader(shown)

        shown.config.targets.forEach { target ->
            val dead = shown.isDead(target)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // Closed by everyone: one clean rule straight across the row, the way a
                    // finished number is scratched off a pub board.
                    .drawWithContent {
                        drawContent()
                        if (dead) {
                            drawLine(
                                color = deadRule,
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = targetLabel(target),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    // Everybody has closed it: struck through, because dimming alone reads as
                    // "not yet reached" rather than "finished with".
                    color = if (dead) Brand.Wire.copy(alpha = 0.5f) else Brand.Chalk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(TargetColumnWidth),
                )
                shown.players.forEachIndexed { index, player ->
                    MarkCell(
                        player = player,
                        target = target,
                        closed = shown.hasClosed(player, target),
                        dead = dead,
                        isThrowing = index == shown.currentPlayerIndex,
                        enabled = enabled,
                        needsDetail = { needsDetail(index, it) },
                        onThrow = { onThrow(index, it) },
                        onPickRing = { onPickRing(index, it) },
                        onPickHouse = { onPickHouse(index) },
                        onClaimHouse = { onClaimHouse(index, it) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreHeader(state: CricketGameState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(Modifier.width(TargetColumnWidth))
        state.players.forEachIndexed { index, player ->
            val isCurrent = index == state.currentPlayerIndex
            // The turn moves by colour and by an underline that grows into place — nothing is
            // scaled, so type stays crisp and the columns never jostle each other.
            val container by animateColorAsState(
                targetValue = if (isCurrent) Brand.Gold else Brand.Key,
                animationSpec = tween(TURN_ANIMATION_MILLIS),
                label = "headerContainer",
            )
            val nameColor by animateColorAsState(
                targetValue = if (isCurrent) Brand.Ink else Brand.Wire,
                animationSpec = tween(TURN_ANIMATION_MILLIS),
                label = "headerName",
            )
            val scoreColor by animateColorAsState(
                targetValue = if (isCurrent) Brand.Ink else Brand.Chalk.copy(alpha = 0.55f),
                animationSpec = tween(TURN_ANIMATION_MILLIS),
                label = "headerScore",
            )
            val underline by animateFloatAsState(
                targetValue = if (isCurrent) 1f else 0f,
                animationSpec = tween(TURN_ANIMATION_MILLIS),
                label = "headerUnderline",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(container)
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = player.player.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = nameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = player.score.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(UnderlineWidth * underline)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brand.Ink),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarkCell(
    player: CricketPlayerState,
    target: CricketTarget,
    closed: Boolean,
    dead: Boolean,
    isThrowing: Boolean,
    enabled: Boolean,
    needsDetail: (CricketTarget) -> Boolean,
    onThrow: (CricketThrow) -> Unit,
    onPickRing: (CricketTarget) -> Unit,
    onPickHouse: () -> Unit,
    onClaimHouse: (List<Dart>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val marks = player.marksOn(target)
    val options = cellOptions(target)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    // A dead row flattens into the board: nothing here is worth aiming at.
                    dead -> Brand.Panel.copy(alpha = 0.6f)
                    isThrowing -> Brand.Key
                    else -> Brand.Key.copy(alpha = 0.55f)
                },
            )
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    when (target) {
                        is CricketTarget.Number -> onThrow(
                            CricketThrow(Dart.Segment(target.value, Ring.SINGLE), target),
                        )
                        CricketTarget.Bull -> onThrow(CricketThrow(Dart.Bull, target))
                        CricketTarget.AnyDouble, CricketTarget.AnyTriple ->
                            if (needsDetail(target)) {
                                onPickRing(target)
                            } else {
                                onThrow(CricketThrow(anonymousRingDart(target), target))
                            }
                        CricketTarget.House ->
                            if (needsDetail(target)) onPickHouse() else onClaimHouse(anonymousHouse())
                    }
                },
                onLongClick = { if (options.isNotEmpty()) expanded = true },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = markGlyph(marks, closed),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                dead -> Brand.Wire.copy(alpha = 0.4f)
                marks == 0 -> Brand.Wire.copy(alpha = 0.45f)
                else -> Brand.Chalk
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onThrow(CricketThrow(option.dart, target))
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * A stand-in dart for a ring mark whose number nobody needs to know.
 *
 * Only used while the mark cannot score, which is exactly when the number has no effect on the
 * game — the board asks for the real one the moment it starts to matter. The slot above still
 * reads as a plain "D" or "T", so nothing invented is ever shown.
 */
private fun anonymousRingDart(target: CricketTarget): Dart =
    if (target == CricketTarget.AnyTriple) {
        Dart.Segment(1, Ring.TRIPLE)
    } else {
        Dart.Segment(1, Ring.DOUBLE)
    }

/**
 * Three darts on one target, standing in for a house whose numbers nobody needs.
 *
 * Like [anonymousRingDart], only used while the house cannot score — the mark is one either
 * way, and the board asks for the real darts the moment their total starts to matter.
 */
private fun anonymousHouse(): List<Dart> = List(3) { Dart.Segment(1, Ring.SINGLE) }

/** How a staged throw reads in the turn slots. */
fun cricketSlotLabel(thrown: CricketThrow): String = when (thrown.target) {
    // The number behind a ring mark is not always known, and never matters to the mark.
    CricketTarget.AnyDouble -> "D"
    CricketTarget.AnyTriple -> "T"
    else -> dartLabel(thrown.dart)
}

/** What a long press offers. Ring and house targets have nothing to offer — they ask instead. */
@Composable
private fun cellOptions(target: CricketTarget): List<CellOption> {
    val doubleWord = stringResource(Res.string.label_double)
    val tripleWord = stringResource(Res.string.label_triple)
    val doubleBullWord = stringResource(Res.string.label_double_bull)
    return when (target) {
        is CricketTarget.Number -> listOf(
            CellOption("D", "$doubleWord ${target.value}", Dart.Segment(target.value, Ring.DOUBLE)),
            CellOption("T", "$tripleWord ${target.value}", Dart.Segment(target.value, Ring.TRIPLE)),
        )
        CricketTarget.Bull -> listOf(CellOption("D", "$doubleBullWord (50)", Dart.DoubleBull))
        else -> emptyList()
    }
}

/** How a target is written down the left-hand column. */
@Composable
fun targetLabel(target: CricketTarget): String = when (target) {
    is CricketTarget.Number -> target.value.toString()
    CricketTarget.Bull -> stringResource(Res.string.label_bull)
    CricketTarget.AnyDouble -> stringResource(Res.string.label_double)
    CricketTarget.AnyTriple -> stringResource(Res.string.label_triple)
    CricketTarget.House -> stringResource(Res.string.label_house)
}

/**
 * The pub-board shorthand. Closed always shows the ringed cross, whatever the match set the
 * requirement to, so a one-mark target still reads as finished.
 */
private fun markGlyph(marks: Int, closed: Boolean): String = when {
    closed -> "⊗"
    marks <= 0 -> "·"
    marks == 1 -> "/"
    else -> "✕"
}
