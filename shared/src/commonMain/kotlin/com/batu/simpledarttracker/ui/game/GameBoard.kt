package com.batu.simpledarttracker.ui.game

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.batu.simpledarttracker.domain.engine.X01Engine
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.GameStatus
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.domain.model.TurnOutcome
import com.batu.simpledarttracker.domain.model.X01GameState
import com.batu.simpledarttracker.domain.model.X01PlayerState
import com.batu.simpledarttracker.ui.common.BackIcon
import com.batu.simpledarttracker.ui.theme.Brand
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.stringResource
import simpledarttracker.shared.generated.resources.Res
import simpledarttracker.shared.generated.resources.action_confirm
import simpledarttracker.shared.generated.resources.action_continue
import simpledarttracker.shared.generated.resources.action_home
import simpledarttracker.shared.generated.resources.action_undo
import simpledarttracker.shared.generated.resources.cd_back
import simpledarttracker.shared.generated.resources.game_continue_question
import simpledarttracker.shared.generated.resources.game_winner
import simpledarttracker.shared.generated.resources.label_bull
import simpledarttracker.shared.generated.resources.label_bust
import simpledarttracker.shared.generated.resources.label_checkout
import simpledarttracker.shared.generated.resources.label_double
import simpledarttracker.shared.generated.resources.label_double_bull
import simpledarttracker.shared.generated.resources.label_miss
import simpledarttracker.shared.generated.resources.label_triple
import simpledarttracker.shared.generated.resources.out_double
import simpledarttracker.shared.generated.resources.out_straight

// Arena color (brand).
private val KeyBg = Color(0xFF243129)

// Page size that fits about three player headers at once.
private val ThreeUpPageSize = object : PageSize {
    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int =
        (availableSpace - 2 * pageSpacing) / 3
}

/**
 * The interactive X01 board — a dark "arena". The dart pad stays **fixed**; only the strip
 * of player headers (name + score) above it scrolls: the player in turn sits centred and
 * highlighted, the neighbours dimmed at the sides (about three visible at once). Throwing:
 * tap a number for a single, hit the adjacent D/T for a double/triple (long press also
 * opens a menu).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoard(
    state: X01GameState,
    onStateChange: (X01GameState) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outcome = X01Engine.outcomeOf(state)
    val outLabel = stringResource(
        if (state.config.doubleOut) Res.string.out_double else Res.string.out_straight,
    )
    val pagerState = rememberPagerState(initialPage = state.currentPlayerIndex) { state.players.size }

    LaunchedEffect(state.currentPlayerIndex) {
        pagerState.animateScrollToPage(state.currentPlayerIndex, animationSpec = tween(durationMillis = 420))
    }

    Scaffold(
        modifier = modifier,
        containerColor = Brand.Slate,
        topBar = {
            TopAppBar(
                title = { Text("${state.config.startingScore} · $outLabel") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        BackIcon(
                            color = Brand.Chalk,
                            contentDescription = stringResource(Res.string.cd_back),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Brand.Slate,
                    titleContentColor = Brand.Chalk,
                ),
            )
        },
        bottomBar = {
            TurnControls(
                state = state,
                outcome = outcome,
                onUndo = { onStateChange(X01Engine.undoLastDart(state)) },
                onConfirm = { onStateChange(X01Engine.confirmTurn(state)) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Scrolling player headers (name + score). This strip is the only thing that moves.
            HorizontalPager(
                state = pagerState,
                pageSize = ThreeUpPageSize,
                pageSpacing = 8.dp,
                snapPosition = SnapPosition.Center,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxWidth().height(96.dp).padding(top = 8.dp),
            ) { page ->
                val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    .absoluteValue.coerceIn(0f, 1f)
                val isCurrent = page == state.currentPlayerIndex
                PlayerHeaderCard(
                    player = state.players[page],
                    isCurrent = isCurrent,
                    remaining = if (isCurrent && outcome != TurnOutcome.BUST) state.currentRemaining
                    else state.players[page].remaining,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 3.dp)
                        .graphicsLayer { alpha = lerp(0.4f, 1f, 1f - offset) },
                )
            }

            // Fixed dart pad.
            DartPad(
                enabled = outcome == TurnOutcome.ONGOING && state.status == GameStatus.IN_PROGRESS,
                onDart = { onStateChange(X01Engine.throwDart(state, it)) },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }

    val winner = state.winner
    if (winner != null) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Brand.Slate2,
            titleContentColor = Brand.Chalk,
            textContentColor = Brand.Chalk,
            confirmButton = {
                TextButton(
                    onClick = { onStateChange(X01Engine.newGame(state.config, state.players.map { it.player })) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Brand.Spruce),
                ) {
                    Text(stringResource(Res.string.action_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onExit,
                    colors = ButtonDefaults.textButtonColors(contentColor = Brand.Chalk),
                ) {
                    Text(stringResource(Res.string.action_home))
                }
            },
            title = { Text("🎯 ${winner.name} ${stringResource(Res.string.game_winner)}") },
            text = { Text(stringResource(Res.string.game_continue_question)) },
        )
    }
}

@Composable
private fun PlayerHeaderCard(
    player: X01PlayerState,
    isCurrent: Boolean,
    remaining: Int,
    modifier: Modifier = Modifier,
) {
    val container = if (isCurrent) Brand.Spruce else KeyBg
    val nameColor = if (isCurrent) Brand.Chalk else Brand.Wire
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = player.player.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = nameColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = remaining.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Brand.Chalk,
        )
    }
}

/** The D/T section of a dart key. */
private data class KeyOption(
    val letter: String,
    val color: Color,
    val dart: Dart,
    val menuLabel: String,
)

@Composable
private fun DartPad(enabled: Boolean, onDart: (Dart) -> Unit, modifier: Modifier = Modifier) {
    val doubleWord = stringResource(Res.string.label_double)
    val tripleWord = stringResource(Res.string.label_triple)
    val doubleBullWord = stringResource(Res.string.label_double_bull)
    val bullWord = stringResource(Res.string.label_bull)
    val missWord = stringResource(Res.string.label_miss)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (20 downTo 1).chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                pair.forEach { n ->
                    DartKey(
                        mainLabel = n.toString(),
                        enabled = enabled,
                        single = Dart.Segment(n, Ring.SINGLE),
                        options = listOf(
                            KeyOption("D", Brand.Spruce, Dart.Segment(n, Ring.DOUBLE), "$doubleWord (${n * 2})"),
                            KeyOption("T", Brand.Bull, Dart.Segment(n, Ring.TRIPLE), "$tripleWord (${n * 3})"),
                        ),
                        onDart = onDart,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DartKey(
                mainLabel = bullWord,
                enabled = enabled,
                single = Dart.Bull,
                options = listOf(KeyOption("D", Brand.Spruce, Dart.DoubleBull, "$doubleBullWord (50)")),
                onDart = onDart,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            DartKey(
                mainLabel = missWord,
                enabled = enabled,
                single = Dart.Miss,
                options = emptyList(),
                onDart = onDart,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DartKey(
    mainLabel: String,
    enabled: Boolean,
    single: Dart,
    options: List<KeyOption>,
    onDart: (Dart) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier.clip(RoundedCornerShape(12.dp))) {
        // Single (tap) plus the long-press menu.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (enabled) KeyBg else KeyBg.copy(alpha = 0.4f))
                .combinedClickable(
                    enabled = enabled,
                    onClick = { onDart(single) },
                    onLongClick = { if (options.isNotEmpty()) expanded = true },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = mainLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Brand.Chalk else Brand.Wire,
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.menuLabel) },
                        onClick = { onDart(opt.dart); expanded = false },
                    )
                }
            }
        }
        // Adjacent D / T sections.
        options.forEach { opt ->
            Box(Modifier.width(1.5.dp).fillMaxHeight().background(Brand.Slate))
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .fillMaxHeight()
                    .background(if (enabled) opt.color else opt.color.copy(alpha = 0.35f))
                    .clickable(enabled = enabled) { onDart(opt.dart) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = opt.letter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Brand.Chalk,
                )
            }
        }
    }
}

@Composable
private fun TurnControls(
    state: X01GameState,
    outcome: TurnOutcome,
    onUndo: () -> Unit,
    onConfirm: () -> Unit,
) {
    Surface(color = Brand.Slate2, contentColor = Brand.Chalk) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { i ->
                    DartSlot(
                        label = state.currentDarts.getOrNull(i)?.let { dartLabel(it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                when (outcome) {
                    TurnOutcome.BUST -> OutcomeBadge(stringResource(Res.string.label_bust), Brand.Bull)
                    TurnOutcome.CHECKOUT -> OutcomeBadge(stringResource(Res.string.label_checkout), Brand.Spruce)
                    else -> {}
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onUndo,
                    enabled = state.currentDarts.isNotEmpty(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand.Chalk),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.action_undo))
                }
                Button(
                    onClick = onConfirm,
                    enabled = outcome != TurnOutcome.ONGOING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brand.Spruce,
                        contentColor = Brand.Chalk,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.action_confirm))
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
            .background(KeyBg),
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

@Composable
private fun OutcomeBadge(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

private fun dartLabel(dart: Dart): String = when (dart) {
    is Dart.Segment -> when (dart.ring) {
        Ring.SINGLE -> dart.number.toString()
        Ring.DOUBLE -> "D${dart.number}"
        Ring.TRIPLE -> "T${dart.number}"
    }
    Dart.Bull -> "25"
    Dart.DoubleBull -> "50"
    Dart.Miss -> "0"
}
