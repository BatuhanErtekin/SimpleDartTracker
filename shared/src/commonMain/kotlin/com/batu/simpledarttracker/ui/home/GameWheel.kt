package com.batu.simpledarttracker.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batu.simpledarttracker.domain.game.GameMode
import com.batu.simpledarttracker.ui.theme.Brand
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/** One slice of the wheel: a game, its name, and the colour it owns. */
data class WheelSector(
    val mode: GameMode,
    val label: String,
    val color: Color,
)

// Proportions of the board, as fractions of its radius.
private const val FACE = 0.86f       // where the playing face ends and the rim begins
private const val RIM = 0.93f        // centre of the dashed rim
private const val INNER = 0.58f      // split between the inner and outer halves of a slice
private const val HUB = 0.115f       // outer bull
private const val EYE = 0.05f        // inner bull
private const val LABEL_RADIUS = 0.55f
private const val RIM_DASHES = 20
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
            Text(
                text = sector.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                // The slices are filled accents, so their type is dark like everywhere else.
                color = Brand.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (cos(angle) * radiusPx * LABEL_RADIUS).roundToInt(),
                            y = (sin(angle) * radiusPx * LABEL_RADIUS).roundToInt(),
                        )
                    }
                    .widthIn(max = 108.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(sector.mode) },
                    )
                    .padding(4.dp)
                    .semantics { contentDescription = sector.label },
            )
        }
    }
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
    if (distance > radius * FACE || distance < radius * HUB) return null

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
    val face = radius * FACE

    drawCircle(Brand.Slate2, radius, centre)

    sectors.forEachIndexed { index, sector ->
        val start = firstEdge + sweep * index
        val lift = press[index]

        // A slice is split into an inner and an outer half, the way a board separates its
        // inner and outer singles. Two flat tones with a wire between them, rather than a band
        // shaded over the top — shading on a saturated colour just reads as a smudge.
        val outer = lerp(sector.color, Color.Black, 0.16f - 0.16f * lift)
        val inner = lerp(sector.color, Color.White, 0.14f + 0.16f * lift)
        listOf(face to outer, radius * INNER to inner).forEach { (extent, tone) ->
            drawArc(
                color = tone,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(centre.x - extent, centre.y - extent),
                size = Size(extent * 2, extent * 2),
            )
        }
    }

    // Wire along the inner/outer split, and between the slices.
    drawCircle(Brand.Chalk.copy(alpha = 0.55f), radius * INNER, centre, style = Stroke(radius * 0.012f))

    // Spider wires between the slices.
    repeat(sectors.size) { index ->
        val angle = ((firstEdge + sweep * index) * PI / 180f).toFloat()
        drawLine(
            color = Brand.Slate,
            start = centre,
            end = Offset(centre.x + cos(angle) * face, centre.y + sin(angle) * face),
            strokeWidth = radius * 0.022f,
        )
    }

    // Rim: a dashed ring standing in for the numbers, between two hairlines.
    val dash = 2f * PI.toFloat() * radius * RIM / (RIM_DASHES * 2f)
    drawCircle(
        color = Brand.Chalk,
        radius = radius * RIM,
        center = centre,
        style = Stroke(
            width = radius * 0.085f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
        ),
    )
    drawCircle(Brand.Chalk, face, centre, style = Stroke(radius * 0.016f))
    drawCircle(Brand.Chalk.copy(alpha = 0.7f), radius * 0.995f, centre, style = Stroke(radius * 0.012f))

    // Bull.
    drawCircle(Brand.Green, radius * HUB, centre)
    drawCircle(Brand.Chalk, radius * HUB, centre, style = Stroke(radius * 0.014f))
    drawCircle(Brand.Red, radius * EYE, centre)
}
