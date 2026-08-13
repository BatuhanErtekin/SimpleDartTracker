package com.batu.simpledarttracker.ui.x01

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.game.TurnOutcome
import com.batu.simpledarttracker.domain.game.winner
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.domain.x01.X01Engine
import com.batu.simpledarttracker.domain.x01.X01GameState
import com.batu.simpledarttracker.ui.common.AppScaffold
import com.batu.simpledarttracker.ui.common.MoreIcon
import com.batu.simpledarttracker.ui.game.DartKeySpec
import com.batu.simpledarttracker.ui.game.DartPad
import com.batu.simpledarttracker.ui.game.dartLabel
import com.batu.simpledarttracker.ui.game.GameSettingsDialog
import com.batu.simpledarttracker.ui.game.KEY_WIDE_OPTION_WEIGHT
import com.batu.simpledarttracker.ui.game.KeyOption
import com.batu.simpledarttracker.ui.game.OutcomeBadge
import com.batu.simpledarttracker.ui.game.PlayerHeaders
import com.batu.simpledarttracker.ui.game.TurnControls
import com.batu.simpledarttracker.ui.game.WinnerDialog
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cd_open_settings
import simpledarttracker.shared.generated.resources.label_bull
import simpledarttracker.shared.generated.resources.label_bust
import simpledarttracker.shared.generated.resources.label_checkout
import simpledarttracker.shared.generated.resources.label_double
import simpledarttracker.shared.generated.resources.label_double_bull
import simpledarttracker.shared.generated.resources.label_miss
import simpledarttracker.shared.generated.resources.label_triple
import simpledarttracker.shared.generated.resources.out_double
import simpledarttracker.shared.generated.resources.out_straight

/**
 * The X01 board — the dark "arena". It is a thin composition of the shared board pieces: the
 * player header strip, the dart pad, and the turn controls. Everything X01-specific lives
 * here: which keys the pad shows, what the headline number means, and how a resolved turn
 * is labelled.
 */
@Composable
fun X01Board(
    state: X01GameState,
    lastTurn: X01GameState?,
    canUndo: Boolean,
    onDart: (Dart) -> Unit,
    onUndo: () -> Unit,
    onPlayAgain: () -> Unit,
    onMovePlayer: (Int, Int) -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outcome = X01Engine.outcomeOf(state)
    // While the turn is empty the panel keeps showing the one that just landed, so a bust is
    // still readable after the throw has passed on.
    val shown = if (state.currentDarts.isEmpty()) lastTurn ?: state else state
    val shownOutcome = X01Engine.outcomeOf(shown)
    val outLabel = stringResource(
        if (state.config.doubleOut) Res.string.out_double else Res.string.out_straight,
    )
    var showSettings by rememberSaveable { mutableStateOf(false) }

    AppScaffold(
        title = "${state.config.startingScore} · $outLabel",
        modifier = modifier,
        containerColor = Brand.Slate,
        contentColor = Brand.Chalk,
        // Mid-match the top-left is settings, not back: leaving is the system back gesture's
        // job, so the corner can hold something useful during play.
        navigationIcon = {
            IconButton(onClick = { showSettings = true }) {
                MoreIcon(
                    color = Brand.Chalk,
                    contentDescription = stringResource(Res.string.cd_open_settings),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        bottomBar = {
            TurnControls(
                slots = shown.currentDarts.map(::dartLabel),
                badge = when (shownOutcome) {
                    TurnOutcome.BUST -> OutcomeBadge(stringResource(Res.string.label_bust), Brand.Red)
                    TurnOutcome.WIN -> OutcomeBadge(stringResource(Res.string.label_checkout), Brand.Green)
                    else -> null
                },
                canUndo = canUndo,
                onUndo = onUndo,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PlayerHeaders(
                state = state,
                headline = { index ->
                    // The player in turn shows the live score, unless the turn busted and
                    // reverts on confirm.
                    val remaining =
                        if (index == state.currentPlayerIndex && outcome != TurnOutcome.BUST) {
                            state.currentRemaining
                        } else {
                            state.players[index].remaining
                        }
                    remaining.toString()
                },
                modifier = Modifier.fillMaxWidth().height(96.dp).padding(top = 8.dp),
            )
            DartPad(
                keys = x01Keys(),
                enabled = outcome == TurnOutcome.ONGOING && state.status == GameStatus.IN_PROGRESS,
                onSelect = onDart,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }

    if (showSettings) {
        GameSettingsDialog(
            players = state.players.map { it.player },
            currentIndex = state.currentPlayerIndex,
            onMovePlayer = onMovePlayer,
            onDismiss = { showSettings = false },
        )
    }

    val winner = state.winner
    if (winner != null) {
        WinnerDialog(winnerName = winner.name, onPlayAgain = onPlayAgain, onHome = onHome)
    }
}

/**
 * Every bed on the board, read down the columns — 20..11 on the left, 10..1 on the right —
 * rather than snaking across the rows, so the eye can follow one column to find a value.
 * Bull and Miss close the pad.
 */
@Composable
private fun x01Keys(): List<DartKeySpec<Dart>> {
    val doubleWord = stringResource(Res.string.label_double)
    val tripleWord = stringResource(Res.string.label_triple)
    val doubleBullWord = stringResource(Res.string.label_double_bull)
    val bullWord = stringResource(Res.string.label_bull)
    val missWord = stringResource(Res.string.label_miss)

    return buildList {
        (20 downTo 11).zip(10 downTo 1).forEach { (left, right) ->
            listOf(left, right).forEach { n ->
                add(
                    DartKeySpec(
                        label = n.toString(),
                        value = Dart.Segment(n, Ring.SINGLE),
                        options = listOf(
                            KeyOption("D", Brand.Green, Dart.Segment(n, Ring.DOUBLE), "$doubleWord (${n * 2})"),
                            KeyOption("T", Brand.Red, Dart.Segment(n, Ring.TRIPLE), "$tripleWord (${n * 3})"),
                        ),
                    ),
                )
            }
        }
        add(
            DartKeySpec(
                label = bullWord,
                value = Dart.Bull,
                options = listOf(
                    KeyOption("D", Brand.Green, Dart.DoubleBull, "$doubleBullWord (50)", KEY_WIDE_OPTION_WEIGHT),
                ),
            ),
        )
        add(DartKeySpec(label = missWord, value = Dart.Miss))
    }
}
