package com.batu.simpledarttracker.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batu.simpledarttracker.domain.game.GameMode
import com.batu.simpledarttracker.ui.brand.Darts
import com.batu.simpledarttracker.ui.theme.Brand
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** One slice of the wheel: a game, its name, and the colour it owns. */
data class WheelSector(
    val mode: GameMode,
    val label: String,
    val color: Color,
)

// Proportions of the board, as fractions of its radius — a real board's, near enough: seven
// segments to a third, the double band at the rim and the treble halfway in.
private const val FACE = 0.95f           // the outer edge of a segment
private const val SEG_IN = 0.17f         // where a segment starts, at the bull's seat
private const val DOUBLE_IN = 0.81f      // the double band, from here out to FACE
private const val TREBLE_IN = 0.49f      // the treble band...
private const val TREBLE_OUT = 0.57f     // ...and where it ends
private const val SEGMENTS = 7           // segments in one third of the board
private const val GAP = 8f               // degrees of board taken out between two thirds
private const val EXPLODE = 0.11f        // how far each third is pushed out from the centre
private const val SEAT = 0.17f           // the disc the bull sits on, and the wheel's dead centre
private const val OUTER_BULL = 0.12f
private const val OUTER_BULL_WIDTH = 0.08f
private const val EYE = 0.055f
private const val WIRE_WIDTH = 0.006f
private const val PRESSED_LIFT = 0.22f   // how much a held third brightens
private const val LABEL_RADIUS = 0.70f   // where the name plate sits on its third
private const val PRESS_MILLIS = 140

/**
 * The mode chooser, drawn as a dartboard cut into one slice per game.
 *
 * Tapping anywhere in a slice picks it — the whole wedge is the target, not just the word — and
 * the slice lifts in colour while held. The labels are also clickable in their own right, which
 * is what gives a screen reader something to land on: a canvas has no shape a reader can find.
 */
@Composable
fun GameWheel(
    sectors: List<WheelSector>,
    onSelect: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf<Int?>(null) }
    val presses: List<State<Float>> = sectors.indices.map { index ->
        animateFloatAsState(
            targetValue = if (pressed == index) 1f else 0f,
            animationSpec = tween(PRESS_MILLIS),
            label = "wheelPress$index",
        )
    }
    val sweep = 360f / sectors.size
    val firstEdge = -90f - sweep / 2f

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val radiusPx = with(LocalDensity.current) { (maxWidth / 2).toPx() }
        val lifts = presses.map { it.value }

        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(sectors.size) {
                    val radius = minOf(size.width, size.height) / 2f
                    val centre = Offset(size.width / 2f, size.height / 2f)
                    detectTapGestures(
                        onPress = { at ->
                            val index = sectorAt(at, radius, centre, firstEdge, sweep)
                            if (index != null) {
                                pressed = index
                                tryAwaitRelease()
                                pressed = null
                            }
                        },
                        onTap = { at ->
                            sectorAt(at, radius, centre, firstEdge, sweep)
                                ?.let { onSelect(sectors[it].mode) }
                        },
                    )
                },
        ) {
            drawBoard(sectors, lifts, firstEdge, sweep)
        }

        sectors.forEachIndexed { index, sector ->
            val angle = ((firstEdge + sweep * index + sweep / 2f) * PI / 180f).toFloat()
            var box by remember { mutableStateOf(IntSize.Zero) }
            Text(
                text = sector.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = sector.color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .onSizeChanged { box = it }
                    .offset { labelOffset(angle, radiusPx, box) }
                    .widthIn(max = 108.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(sector.mode) },
                    )
                    // The name sits on a plate of the board's own dark, in its game's colour:
                    // the segments under it are that colour at full strength, and cream on them
                    // measured 2.7:1.
                    .background(Brand.Board, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .semantics { contentDescription = sector.label },
            )
        }
    }
}

/**
 * Where a third's name plate sits: on the third's own middle, pushed out with the piece it
 * belongs to, and pulled back in far enough that the whole plate stays on the segments.
 */
private fun labelOffset(angle: Float, radius: Float, box: IntSize): IntOffset {
    val reach = abs(cos(angle)) * box.width / 2f + abs(sin(angle)) * box.height / 2f
    val distance = min(radius * (LABEL_RADIUS + EXPLODE), radius * (FACE - 0.06f) - reach)
    return IntOffset((cos(angle) * distance).roundToInt(), (sin(angle) * distance).roundToInt())
}

/** Which slice a tap landed in, or null for the rim, the bull, or outside the board. */
private fun sectorAt(
    at: Offset,
    radius: Float,
    centre: Offset,
    firstEdge: Float,
    sweep: Float,
): Int? {
    val dx = at.x - centre.x
    val dy = at.y - centre.y
    val distance = hypot(dx, dy)
    if (distance > radius * (FACE + EXPLODE) || distance < radius * SEAT) return null

    val degrees = atan2(dy, dx) * 180f / PI.toFloat()
    val fromFirstEdge = ((degrees - firstEdge) % 360f + 360f) % 360f
    return (fromFirstEdge / sweep).toInt()
}

private fun DrawScope.drawBoard(
    sectors: List<WheelSector>,
    press: List<Float>,
    firstEdge: Float,
    sweep: Float,
) {
    val radius = size.minDimension / 2f
    val centre = Offset(size.width / 2f, size.height / 2f)

    sectors.forEachIndexed { index, sector ->
        val start = firstEdge + sweep * index + GAP / 2f
        val span = sweep - GAP
        val lift = press[index]

        // Each third is pushed out along its own middle, so the three read as three objects
        // rather than as one disc with colours painted on it. Held down, a third pushes out a
        // little further and brightens.
        val axis = ((start + span / 2f) * PI / 180f).toFloat()
        val push = radius * (EXPLODE + 0.02f * lift)
        val hub = Offset(centre.x + cos(axis) * push, centre.y + sin(axis) * push)
        val colour = lerp(sector.color, Brand.Chalk, PRESSED_LIFT * lift)

        // Seven segments: the light ones take the game's colour, the dark ones stay board.
        val step = span / SEGMENTS
        repeat(SEGMENTS) { i ->
            val a = start + i * step
            band(hub, radius, SEG_IN, FACE, a, step, if (i % 2 == 0) Brand.Board else colour)
            band(hub, radius, DOUBLE_IN, FACE, a, step, colour)          // double
            band(hub, radius, TREBLE_IN, TREBLE_OUT, a, step, colour)    // treble
            wire(hub, radius, a, wireOn(sector.color))
        }
        listOf(SEG_IN, TREBLE_IN, TREBLE_OUT, DOUBLE_IN, FACE).forEach { r ->
            arcWire(hub, radius, r, start, span, wireOn(sector.color))
        }
    }

    // The bull sits at the true centre, between the three pieces.
    drawCircle(Brand.Board, radius * SEAT, centre)
    drawCircle(Brand.Gold, radius * OUTER_BULL, centre, style = Stroke(radius * OUTER_BULL_WIDTH))
    drawCircle(Brand.Ember, radius * EYE, centre)
}

/** One segment of a third, between two radii. */
private fun DrawScope.band(
    hub: Offset,
    radius: Float,
    from: Float,
    to: Float,
    start: Float,
    span: Float,
    colour: Color,
) {
    val outer = radius * to
    val path = Path().apply {
        arcTo(Rect(hub - Offset(outer, outer), hub + Offset(outer, outer)), start, span, true)
        val inner = radius * from
        arcTo(Rect(hub - Offset(inner, inner), hub + Offset(inner, inner)), start + span, -span, false)
        close()
    }
    drawPath(path, colour)
}

/**
 * The wire between two segments, in [colour].
 *
 * A board's spider is one metal all the way round, but three thirds in three colours cannot all
 * carry the same wire: gold on gold disappears. Each third borrows the colour that shows on it —
 * see [wireOn].
 */
private fun DrawScope.wire(hub: Offset, radius: Float, at: Float, colour: Color) {
    val a = (at * PI / 180f).toFloat()
    drawLine(
        color = colour,
        start = Offset(hub.x + cos(a) * radius * SEG_IN, hub.y + sin(a) * radius * SEG_IN),
        end = Offset(hub.x + cos(a) * radius * FACE, hub.y + sin(a) * radius * FACE),
        strokeWidth = radius * WIRE_WIDTH,
        alpha = 0.9f,
    )
}

/** The colour a third's wire takes: the one of the other two that actually reads on it. */
private fun wireOn(slice: Color): Color = when (slice) {
    Brand.Gold -> Brand.Ember      // red on yellow
    Brand.Ember -> Brand.Chalk     // cream on red
    else -> Brand.Gold             // yellow on cream
}

/** The wire that runs round a third at one radius, in the same [colour] as its spokes. */
private fun DrawScope.arcWire(
    hub: Offset,
    radius: Float,
    at: Float,
    start: Float,
    span: Float,
    colour: Color,
) {
    val r = radius * at
    drawArc(
        color = colour,
        startAngle = start,
        sweepAngle = span,
        useCenter = false,
        topLeft = Offset(hub.x - r, hub.y - r),
        size = Size(r * 2f, r * 2f),
        alpha = 0.9f,
        style = Stroke(radius * WIRE_WIDTH * 1.2f),
    )
}

