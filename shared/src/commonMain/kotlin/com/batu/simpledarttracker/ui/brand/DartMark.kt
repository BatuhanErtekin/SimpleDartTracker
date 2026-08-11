package com.batu.simpledarttracker.ui.brand

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate

/**
 * The Simple Dart Tracker logo: a bullseye reduced from a dartboard (thin outer circle,
 * a dashed ring hinting at the 20 segments, a green inner ring, a red bull) with a dart
 * stuck in the bull at 45°. Scaled inside a 100x100 design square.
 *
 * [ringsAppear] and [dartAppear] run from 0 to 1; the splash animation uses them to settle
 * the rings into place and fly the dart in (both default to 1 = fully visible).
 */
@Composable
fun DartMark(
    lineColor: Color,
    greenColor: Color,
    redColor: Color,
    haloColor: Color,
    modifier: Modifier = Modifier,
    ringsAppear: Float = 1f,
    dartAppear: Float = 1f,
) {
    Canvas(modifier) {
        val unit = size.minDimension / 100f
        val offX = (size.width - 100f * unit) / 2f
        val offY = (size.height - 100f * unit) / 2f
        fun pt(x: Float, y: Float) = Offset(offX + x * unit, offY + y * unit)
        val center = pt(50f, 50f)

        // Rings: settle into place by scaling up slightly while fading in.
        val ringsScale = 0.82f + 0.18f * ringsAppear
        scale(ringsScale, ringsScale, center) {
            drawCircle(lineColor, 38f * unit, center, alpha = ringsAppear, style = Stroke(1.6f * unit))
            drawCircle(
                lineColor, 34f * unit, center, alpha = ringsAppear,
                style = Stroke(
                    width = 4f * unit,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.34f * unit, 5.34f * unit)),
                ),
            )
            drawCircle(greenColor, 21.5f * unit, center, alpha = ringsAppear, style = Stroke(3.2f * unit))
            drawCircle(greenColor, 8.4f * unit, center, alpha = ringsAppear, style = Stroke(2.4f * unit))
            drawCircle(redColor, 3.8f * unit, center, alpha = ringsAppear)
        }

        // Dart: flies in from the top right (dartAppear=0) and sticks in the bull (dartAppear=1).
        val dx = 28f * unit * (1f - dartAppear)
        val dy = -28f * unit * (1f - dartAppear)
        translate(dx, dy) {
            rotate(-45f, center) {
                // The halo separates the dart from the board behind it.
                drawLine(haloColor, pt(50f, 50f), pt(73f, 50f), strokeWidth = 6.5f * unit, cap = StrokeCap.Round, alpha = dartAppear)
                drawPath(triangle(pt(69f, 50f), pt(90f, 42f), pt(90f, 58f)), haloColor, alpha = dartAppear)
                // Flight and shaft.
                drawPath(triangle(pt(70f, 50f), pt(88f, 43f), pt(88f, 57f)), redColor, alpha = dartAppear)
                drawLine(lineColor, pt(50f, 50f), pt(72f, 50f), strokeWidth = 3f * unit, cap = StrokeCap.Round, alpha = dartAppear)
                drawCircle(lineColor, 2.1f * unit, pt(50f, 50f), alpha = dartAppear)
            }
        }
    }
}

private fun triangle(a: Offset, b: Offset, c: Offset) = Path().apply {
    moveTo(a.x, a.y)
    lineTo(b.x, b.y)
    lineTo(c.x, c.y)
    close()
}
