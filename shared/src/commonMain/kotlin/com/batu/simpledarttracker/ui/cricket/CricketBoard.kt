package com.batu.simpledarttracker.ui.cricket

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.cricket.CricketEngine
import com.batu.simpledarttracker.domain.cricket.CricketGameState
import com.batu.simpledarttracker.domain.cricket.CricketScoring
import com.batu.simpledarttracker.domain.cricket.CricketTarget
import com.batu.simpledarttracker.domain.cricket.CricketThrow
import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.game.TurnOutcome
import com.batu.simpledarttracker.domain.game.winner
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.ui.common.AppScaffold
import com.batu.simpledarttracker.ui.common.MoreIcon
import com.batu.simpledarttracker.ui.game.GameSettingsDialog
import com.batu.simpledarttracker.ui.game.OutcomeBadge
import com.batu.simpledarttracker.ui.game.TurnControls
import com.batu.simpledarttracker.ui.game.WinnerDialog
import com.batu.simpledarttracker.ui.theme.Brand
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.cd_open_settings
import simpledarttracker.shared.generated.resources.cricket_scoring_penalty
import simpledarttracker.shared.generated.resources.cricket_scoring_plain
import simpledarttracker.shared.generated.resources.label_checkout
import simpledarttracker.shared.generated.resources.label_house
import simpledarttracker.shared.generated.resources.mode_cricket

/**
 * The Cricket board. Unlike X01 there is no separate pad: the scoreboard fills the screen and
 * is itself the way darts are entered, which leaves every cell big enough to hit. The shared
 * turn controls sit underneath, and Cricket's own pickers layer on top.
 */
@Composable
fun CricketBoard(
    state: CricketGameState,
    lastTurn: CricketGameState?,
    canUndo: Boolean,
    onThrow: (playerIndex: Int, CricketThrow) -> Unit,
    onClaimHouse: (playerIndex: Int, List<Dart>) -> Unit,
    onUndo: () -> Unit,
    onPlayAgain: () -> Unit,
    onMovePlayer: (Int, Int) -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outcome = remember(state) { CricketEngine.outcomeOf(state) }
    // While the turn is empty the panel keeps showing the one that just landed.
    val shown = if (state.currentThrows.isEmpty()) lastTurn ?: state else state
    val scoringLabel = stringResource(
        when (state.config.scoring) {
            CricketScoring.STANDARD -> Res.string.cricket_scoring_plain
            CricketScoring.CUT_THROAT -> Res.string.cricket_scoring_penalty
        },
    )
    val houseInitial = stringResource(Res.string.label_house).take(1).uppercase()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    // A picker belongs to the column that opened it, so its answer goes to the right player.
    var ringPicker by remember { mutableStateOf<Pair<Int, CricketTarget>?>(null) }
    var housePicker by remember { mutableStateOf<Int?>(null) }

    AppScaffold(
        title = "${stringResource(Res.string.mode_cricket)} · $scoringLabel",
        modifier = modifier,
        containerColor = Brand.Night,
        contentColor = Brand.Chalk,
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
                // A house is one event, not three darts, and its numbers are often not even
                // asked for — so it reads as a house rather than as whatever stood in for it.
                slots = if (shown.houseClaimed) {
                    List(shown.currentThrows.size) { houseInitial }
                } else {
                    shown.currentThrows.map(::cricketSlotLabel)
                },
                badge = when {
                    CricketEngine.outcomeOf(shown) == TurnOutcome.WIN ->
                        OutcomeBadge(stringResource(Res.string.label_checkout), Brand.Gold)
                    shown.houseClaimed ->
                        OutcomeBadge(stringResource(Res.string.label_house), Brand.Ember)
                    else -> null
                },
                canUndo = canUndo,
                onUndo = onUndo,
                // Alone there is no other column to tap, so a short visit still needs a way out.
                onMiss = if (state.players.size == 1) {
                    { onThrow(0, CricketThrow.missed()) }
                } else {
                    null
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            CricketScoreboard(
                state = state,
                enabled = outcome == TurnOutcome.ONGOING && state.status == GameStatus.IN_PROGRESS,
                onThrow = onThrow,
                onPickRing = { player, target -> ringPicker = player to target },
                onPickHouse = { player -> housePicker = player },
                onClaimHouse = onClaimHouse,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }

    ringPicker?.let { (player, target) ->
        RingPickerDialog(
            target = target,
            onPicked = { dart ->
                onThrow(player, CricketThrow(dart, target))
                ringPicker = null
            },
            onDismiss = { ringPicker = null },
        )
    }

    housePicker?.let { player ->
        HousePickerDialog(
            onPicked = { darts ->
                onClaimHouse(player, darts)
                housePicker = null
            },
            onDismiss = { housePicker = null },
        )
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
