package com.batu.simpledarttracker.ui.brand

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * A dart, drawn in the 100x100 design square the marks are built from: it lies along the +x axis
 * with its point towards the smaller x, so drawing it at an angle is one rotation.
 *
 * Both darts here are a Winmau MVG Exact, near enough: a straight tube of a barrel with grip
 * rings cut into it, a thin shaft, and a flight that opens out and closes on a straight edge.
 * That silhouette is what says "dart" at icon size; a line and a triangle only ever read as an
 * arrow, and a flight notched into a V reads as one too.
 *
 * @param half the outline from the point back, on one side of the axis only; every point off the
 *   axis is mirrored to make the other side, so a shape ending on the axis closes to a notch and
 *   one ending off it closes on a straight back edge.
 */
internal class Dart(
    private val half: List<Pair<Float, Float>>,
    val grooves: List<Float>,
    val grooveHalf: Float,
    val grooveWidth: Float,
    /** The dark cut the dart lies in: a stroke of this width around the same outline. */
    val cut: Float = 1.9f,
    /** How far the metal's light-to-dark ramp runs either side of the axis. */
    val halfDepth: Float = 3.4f,
) {
    private val outline: List<Pair<Float, Float>> =
        half + half.asReversed().mapNotNull { (x, dy) -> if (dy == 0f) null else x to -dy }

    /** The dart as a path lying along +x from [centre], at [unit] pixels to a design unit. */
    fun path(centre: Offset, unit: Float): Path = Path().apply {
        outline.forEachIndexed { index, (x, dy) ->
            val px = centre.x + (x - 50f) * unit
            val py = centre.y - dy * unit
            if (index == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    /** The two ends of grip ring [index], for a dart drawn along +x from [centre]. */
    fun groove(index: Int, centre: Offset, unit: Float): Pair<Offset, Offset> {
        val x = centre.x + (grooves[index] - 50f) * unit
        return Offset(x, centre.y - grooveHalf * unit) to Offset(x, centre.y + grooveHalf * unit)
    }
}

internal object Darts {
    /** The wheel's dart: thrown, its point at the bull, cut out of the gap between two slices. */
    val Thrown = Dart(
        half = listOf(
            63.4f to 0f,     // needle tip, just clear of the bull's seat
            68.4f to 0.8f,   // the needle's shoulder
            69.6f to 1.78f,  // the barrel starts
            81.8f to 1.78f,  // ...and runs dead straight to here
            83.0f to 0.95f,  // it steps down into the shaft
            87.2f to 0.88f,
            88.6f to 5.4f,   // the flight opens out
            93.8f to 6.4f,   // its back corner
            90.2f to 0f,     // the notch between the two wings
        ),
        grooves = listOf(73.0f, 75.4f, 77.8f),
        grooveHalf = 1.78f,
        grooveWidth = 0.55f,
        halfDepth = 2.6f,
    )
}
