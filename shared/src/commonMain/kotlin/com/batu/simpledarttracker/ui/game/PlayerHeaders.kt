package com.batu.simpledarttracker.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batu.simpledarttracker.domain.game.GameState
import com.batu.simpledarttracker.ui.theme.Brand

// How many player headers are visible at once.
private const val HEADERS_ABREAST = 3
private val HeaderSpacing = 8.dp
private const val SETTLE_MILLIS = 420
private const val TURN_ANIMATION_MILLIS = 260
private val UnderlineWidth = 28.dp

/**
 * The strip of player headers: the player in turn sits in the middle, neighbours peek in at
 * the sides. It can be swiped to look up someone who is off-screen, and drifts back to the
 * player in turn as soon as the swipe settles.
 *
 * [headline] supplies the big number under each name, so each game can show whatever it
 * scores — a remaining total, a mark count, anything.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerHeaders(
    state: GameState<*>,
    headline: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = state.currentPlayerIndex) { state.players.size }
    // Read through a holder so the settle-back effect below always compares against the
    // current turn without having to restart its collector every turn.
    val turnPage by rememberUpdatedState(state.currentPlayerIndex)

    LaunchedEffect(state.currentPlayerIndex) {
        pagerState.animateScrollToPage(state.currentPlayerIndex, animationSpec = tween(SETTLE_MILLIS))
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling && pagerState.currentPage != turnPage) {
                pagerState.animateScrollToPage(turnPage, animationSpec = tween(SETTLE_MILLIS))
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
                name = state.players[page].player.name,
                headline = headline(page),
                isCurrent = page == state.currentPlayerIndex,
                // Must fill the page: a card sized to its text would sit at the page's
                // leading edge and read as off-centre.
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlayerHeaderCard(
    name: String,
    headline: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    // The turn is carried by colour, a border that fades in, and an underline that grows into
    // place. Nothing is scaled: scaling resamples the type and makes the strip jitter.
    val alpha by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0.45f,
        animationSpec = tween(TURN_ANIMATION_MILLIS),
        label = "playerHeaderAlpha",
    )
    val container by animateColorAsState(
        targetValue = if (isCurrent) Brand.Green else Brand.Key,
        animationSpec = tween(TURN_ANIMATION_MILLIS),
        label = "playerHeaderContainer",
    )
    val nameColor by animateColorAsState(
        targetValue = if (isCurrent) Brand.Ink else Brand.Wire,
        animationSpec = tween(TURN_ANIMATION_MILLIS),
        label = "playerHeaderName",
    )
    val borderColor by animateColorAsState(
        targetValue = Brand.Chalk.copy(alpha = if (isCurrent) 0.45f else 0f),
        animationSpec = tween(TURN_ANIMATION_MILLIS),
        label = "playerHeaderBorder",
    )
    val underline by animateFloatAsState(
        targetValue = if (isCurrent) 1f else 0f,
        animationSpec = tween(TURN_ANIMATION_MILLIS),
        label = "playerHeaderUnderline",
    )
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(shape)
            .background(container)
            .border(2.dp, borderColor, shape)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = nameColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCurrent) Brand.Ink else Brand.Chalk,
        )
        // A bull-red underline marks whose throw it is, for anyone who cannot rely on the
        // green fill alone.
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
