package com.batu.simpledarttracker.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.engine.X01Engine
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.GameStatus
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.domain.model.TurnOutcome
import com.batu.simpledarttracker.domain.model.X01GameState
import com.batu.simpledarttracker.domain.model.X01PlayerState
import com.batu.simpledarttracker.ui.common.BackIcon
import com.batu.simpledarttracker.ui.theme.Brand
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

// How many player headers are visible at once in the carousel.
private const val HEADERS_ABREAST = 3
private val HeaderSpacing = 8.dp

// A key splits its width by weight: the label takes half, the D/T sections a quarter each.
// Bull only has a D, so that one section is given the room of both and the key reads 50/50.
private const val KEY_LABEL_WEIGHT = 2f
private const val KEY_OPTION_WEIGHT = 1f

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
            // Player headers (name + score). The player in turn is always centred.
            PlayerHeaders(
                state = state,
                outcome = outcome,
                modifier = Modifier.fillMaxWidth().height(96.dp).padding(top = 8.dp),
            )

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

/**
 * The strip of player headers: the player in turn sits in the middle, neighbours peek in at
 * the sides. It can be swiped to look up someone who is off-screen, and drifts back to the
 * player in turn as soon as the swipe settles.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerHeaders(
    state: X01GameState,
    outcome: TurnOutcome,
    modifier: Modifier = Modifier,
) {
    // The player in turn shows the live score, unless the turn busted and reverts on confirm.
    fun remainingOf(index: Int): Int =
        if (index == state.currentPlayerIndex && outcome != TurnOutcome.BUST) state.currentRemaining
        else state.players[index].remaining

    val pagerState = rememberPagerState(initialPage = state.currentPlayerIndex) { state.players.size }
    // Read through a holder so the settle-back effect below always compares against the
    // current turn without having to restart its collector every turn.
    val turnPage by rememberUpdatedState(state.currentPlayerIndex)

    LaunchedEffect(state.currentPlayerIndex) {
        pagerState.animateScrollToPage(state.currentPlayerIndex, animationSpec = tween(durationMillis = 420))
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling && pagerState.currentPage != turnPage) {
                pagerState.animateScrollToPage(turnPage, animationSpec = tween(durationMillis = 420))
            }
        }
    }

    BoxWithConstraints(modifier) {
        // Three cards abreast. The side padding is what lets the first and last player settle
        // in the middle too — without it the pager runs out of scroll and they stay off-centre.
        val pageWidth = (maxWidth - HeaderSpacing * 2) / HEADERS_ABREAST
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(pageWidth),
            contentPadding = PaddingValues(horizontal = (maxWidth - pageWidth) / 2),
            pageSpacing = HeaderSpacing,
            snapPosition = SnapPosition.Center,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            PlayerHeaderCard(
                player = state.players[page],
                isCurrent = page == state.currentPlayerIndex,
                remaining = remainingOf(page),
                // Must fill the page: a card sized to its text would sit at the page's
                // leading edge and read as off-centre.
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlayerHeaderCard(
    player: X01PlayerState,
    isCurrent: Boolean,
    remaining: Int,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(if (isCurrent) 1f else 0.45f, label = "playerHeaderAlpha")
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(shape)
            .background(if (isCurrent) Brand.Spruce else KeyBg)
            .then(if (isCurrent) Modifier.border(2.dp, Brand.Chalk.copy(alpha = 0.45f), shape) else Modifier)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = player.player.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isCurrent) Brand.Chalk else Brand.Wire,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = remaining.toString(),
            style = if (isCurrent) MaterialTheme.typography.headlineMedium
            else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Brand.Chalk,
        )
        // A bull-red underline marks whose throw it is, for anyone who cannot rely on the
        // green fill alone.
        if (isCurrent) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.width(28.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Brand.Bull))
        }
    }
}

/** The D/T section of a dart key. */
private data class KeyOption(
    val letter: String,
    val color: Color,
    val dart: Dart,
    val menuLabel: String,
    val weight: Float = KEY_OPTION_WEIGHT,
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
        // Numbers run down each column — 20..11 on the left, 10..1 on the right — rather than
        // snaking across the rows, so the eye can follow one column to find a value.
        (20 downTo 11).zip(10 downTo 1).forEach { (left, right) ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(left, right).forEach { n ->
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
                options = listOf(
                    KeyOption("D", Brand.Spruce, Dart.DoubleBull, "$doubleBullWord (50)", KEY_LABEL_WEIGHT),
                ),
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
                .weight(KEY_LABEL_WEIGHT)
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
                    .weight(opt.weight)
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
            // The surface itself runs to the bottom edge; only its content clears the
            // navigation bar, so the bar sits on the panel colour rather than on the buttons.
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(12.dp),
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
