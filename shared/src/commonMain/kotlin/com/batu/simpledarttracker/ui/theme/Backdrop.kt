package com.batu.simpledarttracker.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

// A bed of coals, in the colours coals actually have: orange where the air gets in, falling to a
// deep ash red at the edges. No tongues — this fire has burned down.
private val Glow = Color(0xFFFF9A2E)
private val Coal = Color(0xFFC43A10)
private val Ash = Color(0xFF7A1E08)
private val Spark = Color(0xFFFFD37A)

private const val RISE = 0.62f          // how far up the screen an ember gets before it dies
private const val CYCLE_MILLIS = 24_000 // one turn of the drift; each ember runs at its own rate

/** One ember: where it lifts off, when in the cycle, how fast it climbs, and how big it is. */
private data class Ember(val at: Float, val seed: Float, val speed: Float, val radius: Float)

// Placed by hand rather than randomised. Random positions would reshuffle every time the page
// recomposed, and a background you notice moving is a background that has stopped being one.
private val EMBERS = listOf(
    Ember(0.08f, 0.00f, 0.9f, 1.6f), Ember(0.21f, 0.31f, 1.2f, 1.1f),
    Ember(0.14f, 0.62f, 0.7f, 2.0f), Ember(0.31f, 0.14f, 1.1f, 1.4f),
    Ember(0.38f, 0.47f, 0.8f, 1.8f), Ember(0.29f, 0.79f, 1.3f, 1.2f),
    Ember(0.46f, 0.22f, 1.0f, 2.2f), Ember(0.52f, 0.55f, 0.75f, 1.3f),
    Ember(0.44f, 0.88f, 1.15f, 1.6f), Ember(0.61f, 0.07f, 0.85f, 1.9f),
    Ember(0.68f, 0.39f, 1.25f, 1.2f), Ember(0.58f, 0.71f, 0.95f, 1.5f),
    Ember(0.74f, 0.18f, 1.05f, 1.7f), Ember(0.83f, 0.50f, 0.8f, 1.1f),
    Ember(0.71f, 0.83f, 1.3f, 2.1f), Ember(0.90f, 0.26f, 0.9f, 1.4f),
    Ember(0.94f, 0.58f, 1.1f, 1.8f), Ember(0.86f, 0.94f, 0.7f, 1.2f),
    Ember(0.12f, 0.43f, 1.2f, 1.3f), Ember(0.36f, 0.66f, 0.85f, 1.5f),
    Ember(0.64f, 0.11f, 1.15f, 1.2f), Ember(0.79f, 0.35f, 0.95f, 1.6f),
    Ember(0.25f, 0.90f, 1.05f, 1.1f), Ember(0.55f, 0.74f, 0.8f, 1.4f),
)

/**
 * The page: a bed of coals at the foot of the screen, with embers lifting off it.
 *
 * It is named for the user's nephew — Ateş. The flames themselves are gone; what is left is the
 * light coming off them, which is the part that never fights the content sitting on top.
 *
 * Only the embers move, and they move slowly: one climb takes the better part of half a minute,
 * and each one runs at its own rate so the field never pulses in step. The glow is fixed. The
 * whole thing is radial gradients — no blur, no filters, nothing that draws differently on one
 * platform than another.
 */
@Composable
fun Modifier.appBackdrop(): Modifier {
    val transition = rememberInfiniteTransition(label = "embers")
    val drift = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(CYCLE_MILLIS, easing = LinearEasing)),
        label = "drift",
    )
    // The value is read inside the draw lambda, so the embers cost a redraw and not a recomposition.
    return drawBehind {
        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFF140E08),
                0.5f to Color(0xFF0F0A06),
                1f to Color(0xFF210C04),
            ),
        )
        coals()
        embers(drift.value)
    }
}

/** The bed itself: three lumps of light on the bottom edge, widest and dimmest first. */
private fun DrawScope.coals() {
    listOf(
        Triple(0.75f, 0.25f, 0.34f),
        Triple(0.45f, 0.16f, 0.30f),
        Triple(0.22f, 0.09f, 0.26f),
    ).forEach { (width, height, strength) ->
        blob(
            centre = Offset(size.width * 0.5f, size.height + size.height * 0.02f),
            radiusX = size.width * width,
            radiusY = size.height * height,
            stops = arrayOf(
                0f to Glow.copy(alpha = strength),
                0.5f to Coal.copy(alpha = strength * 0.5f),
                1f to Ash.copy(alpha = 0f),
            ),
        )
    }
}

/** The embers, climbing and going out. [drift] runs 0 to 1 and wraps. */
private fun DrawScope.embers(drift: Float) {
    EMBERS.forEach { ember ->
        val climb = ((drift * ember.speed + ember.seed) % 1f + 1f) % 1f
        val sway = sin((climb + ember.seed) * 2f * PI.toFloat()) * size.width * 0.018f
        // Bright the moment it leaves the coals, then dimmer the higher and colder it gets.
        val alpha = min(climb * 8f, 1f) * (1f - climb) * 0.85f
        drawCircle(
            color = Spark,
            radius = ember.radius * (1f - 0.35f * climb),
            center = Offset(
                x = size.width * ember.at + sway,
                y = size.height - size.height * RISE * climb,
            ),
            alpha = alpha,
        )
    }
}

/** A soft ellipse of light: a circular gradient, squashed to shape. */
private fun DrawScope.blob(
    centre: Offset,
    radiusX: Float,
    radiusY: Float,
    stops: Array<Pair<Float, Color>>,
) {
    val radius = maxOf(radiusX, radiusY)
    if (radius <= 0f) return
    scale(radiusX / radius, radiusY / radius, centre) {
        drawCircle(
            brush = Brush.radialGradient(colorStops = stops, center = centre, radius = radius),
            radius = radius,
            center = centre,
        )
    }
}
