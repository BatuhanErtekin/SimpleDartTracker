package com.batu.simpledarttracker.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.cricket.CricketConfig
import com.batu.simpledarttracker.domain.cricket.CricketScoring
import com.batu.simpledarttracker.domain.cricket.CricketTarget
import com.batu.simpledarttracker.domain.cricket.StandardCricketTargets
import com.batu.simpledarttracker.domain.cricket.randomCricketTargets
import com.batu.simpledarttracker.domain.game.GameMode
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.x01.X01Config
import com.batu.simpledarttracker.ui.common.AppScaffold
import com.batu.simpledarttracker.ui.common.bottomBarInsets
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_start
import simpledarttracker.shared.generated.resources.cricket_no_targets
import simpledarttracker.shared.generated.resources.setup_unavailable
import simpledarttracker.shared.generated.resources.mode_cricket
import simpledarttracker.shared.generated.resources.mode_training
import simpledarttracker.shared.generated.resources.mode_x01
import simpledarttracker.shared.generated.resources.player_word
import simpledarttracker.shared.generated.resources.players_title

private const val DEFAULT_PLAYERS = 2

/** Targets round-trip through their stable ids, so the picker survives a rotation. */
private val CricketTargetsSaver = listSaver<Set<CricketTarget>, String>(
    save = { targets -> targets.map { it.id } },
    restore = { ids -> ids.mapNotNull { CricketTarget.ofId(it) }.toSet() },
)

/** Mark overrides travel as "id=count" pairs for the same reason. */
private val CricketMarksSaver = listSaver<Map<CricketTarget, Int>, String>(
    save = { marks -> marks.map { (target, count) -> "${target.id}=$count" } },
    restore = { entries ->
        entries.mapNotNull { entry ->
            val (id, count) = entry.split("=", limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
            val target = CricketTarget.ofId(id) ?: return@mapNotNull null
            val marks = count.toIntOrNull() ?: return@mapNotNull null
            target to marks
        }.toMap()
    },
)

/**
 * One screen for everything a match needs, instead of a page per question. The rules section
 * is whatever the chosen [mode] contributes; the player section below it is shared by every
 * mode. [onStart] only fires once the mode can actually produce a setup.
 */
@Composable
fun GameSetupScreen(
    mode: GameMode,
    onBack: () -> Unit,
    onStart: (MatchSetup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerWord = stringResource(Res.string.player_word)
    val defaultName: (Int) -> String = { index -> "$playerWord ${index + 1}" }

    var names by rememberSaveable {
        mutableStateOf(List(DEFAULT_PLAYERS) { "$playerWord ${it + 1}" })
    }
    var preset by rememberSaveable { mutableIntStateOf(PRESET_501) }
    var customScore by rememberSaveable { mutableIntStateOf(CUSTOM_DEFAULT) }
    var doubleOut by rememberSaveable { mutableStateOf(true) }

    var cricketPreset by rememberSaveable { mutableIntStateOf(CRICKET_PRESET_STANDARD) }
    var cricketTargets by rememberSaveable(stateSaver = CricketTargetsSaver) {
        mutableStateOf(StandardCricketTargets)
    }
    var cricketMarks by rememberSaveable(stateSaver = CricketMarksSaver) {
        mutableStateOf(emptyMap<CricketTarget, Int>())
    }
    var cricketScoring by rememberSaveable { mutableIntStateOf(CricketScoring.STANDARD.ordinal) }

    /** Editing targets or mark counts by hand always means the set is no longer a preset. */
    fun toggleTarget(target: CricketTarget) {
        cricketTargets = if (target in cricketTargets) {
            cricketTargets - target
        } else {
            cricketTargets + target
        }
        cricketPreset = CRICKET_PRESET_CUSTOM
    }

    /** Choosing a mark count also brings the target into play, so one tap is enough. */
    fun setMarks(target: CricketTarget, count: Int) {
        cricketMarks = cricketMarks + (target to count)
        cricketTargets = cricketTargets + target
        cricketPreset = CRICKET_PRESET_CUSTOM
    }

    fun applyPreset(targets: Set<CricketTarget>, preset: Int) {
        cricketTargets = targets
        cricketMarks = emptyMap()
        cricketPreset = preset
    }

    val players = names.mapIndexed { index, name ->
        Player(id = "p${index + 1}", name = name.ifBlank { defaultName(index) })
    }
    val setup: MatchSetup? = when (mode) {
        GameMode.X01 -> MatchSetup.X01(
            players = players,
            config = X01Config(
                startingScore = startingScoreOf(preset, customScore),
                doubleOut = doubleOut,
            ),
        )
        GameMode.CRICKET -> cricketTargets
            .takeIf { it.isNotEmpty() }
            ?.let { chosen ->
                MatchSetup.Cricket(
                    players = players,
                    config = CricketConfig(
                        // Keep the board's own order rather than whatever the set iterated in.
                        targets = (CricketTarget.numbers + CricketTarget.extras).filter { it in chosen },
                        markOverrides = cricketMarks.filterKeys { it in chosen },
                        scoring = CricketScoring.entries[cricketScoring],
                    ),
                )
            }
        GameMode.TRAINING -> null
    }

    AppScaffold(
        title = stringResource(
            when (mode) {
                GameMode.X01 -> Res.string.mode_x01
                GameMode.CRICKET -> Res.string.mode_cricket
                GameMode.TRAINING -> Res.string.mode_training
            },
        ),
        modifier = modifier,
        onBack = onBack,
        bottomBar = {
            Button(
                onClick = { setup?.let(onStart) },
                enabled = setup != null,
                modifier = Modifier.fillMaxWidth().bottomBarInsets().padding(16.dp),
            ) {
                Text(stringResource(Res.string.action_start))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (mode) {
                GameMode.X01 -> X01Options(
                    preset = preset,
                    onPresetChange = { preset = it },
                    customScore = customScore,
                    onCustomScoreChange = { customScore = it },
                    doubleOut = doubleOut,
                    onDoubleOutChange = { doubleOut = it },
                )
                GameMode.CRICKET -> CricketOptions(
                    preset = cricketPreset,
                    selectedTargets = cricketTargets,
                    marks = cricketMarks,
                    onStandard = { applyPreset(StandardCricketTargets, CRICKET_PRESET_STANDARD) },
                    onRandom = { applyPreset(randomCricketTargets(), CRICKET_PRESET_RANDOM) },
                    onCustom = { cricketPreset = CRICKET_PRESET_CUSTOM },
                    onToggleTarget = ::toggleTarget,
                    onMarksChange = ::setMarks,
                    scoring = CricketScoring.entries[cricketScoring],
                    onScoringChange = { cricketScoring = it.ordinal },
                )
                GameMode.TRAINING -> Unit
            }

            if (setup == null) {
                Text(
                    text = stringResource(
                        // Cricket is playable; it just needs something to aim at.
                        if (mode == GameMode.CRICKET) {
                            Res.string.cricket_no_targets
                        } else {
                            Res.string.setup_unavailable
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel(stringResource(Res.string.players_title))
            PlayersSection(
                names = names,
                onNamesChange = { names = it },
                defaultName = defaultName,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
