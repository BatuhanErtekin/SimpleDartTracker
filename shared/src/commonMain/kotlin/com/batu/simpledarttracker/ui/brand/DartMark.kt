package com.batu.simpledarttracker.ui.brand

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate

// The mark's own colours. They are deliberately not the app's palette: a logo does not follow
// the theme, and the launcher drawables are cut from these same numbers. The launcher's own
// ground colour is #141009, in ic_launcher_background.xml.
private val Gold = Color(0xFFF0A828)
private val Hoop = Color(0xFF9C5606)
private val Foam = Color(0xFFF3EDDE)
private val Barrel = Color(0xFF1B1B18)
private val Accent = Color(0xFFFFD37A)

// Everything below is measured in the 108x108 square the launcher icon is drawn in.
private const val MARK = 1.12f              // the mark fills the box the ring used to leave room for
private const val DART_AT = -36f            // the dart crosses the tankard on this diagonal
private val GROOVES = listOf(-19f, -14.8f, -10.6f, -6.4f, -2.2f)

/**
 * The Simple Dart Tracker mark: a tankard of beer with a dart driven straight through it.
 *
 * The dart is a Winmau MvG Evo-X — straight parallel barrel, rounded nose, full-length ring grip,
 * gold groove accents — which is where the mark's gold comes from in the first place. The tankard
 * is a barrel-bodied stein with a square handle behind it, drawn flat: no gradients, no outline
 * on the glass, so the whole thing holds together at 32 pixels.
 *
 * [markAppear] and [dartAppear] run from 0 to 1; the splash animation settles the tankard into
 * place with the first and drives the dart in along its own diagonal with the second.
 */
@Composable
fun DartMark(
    modifier: Modifier = Modifier,
    markAppear: Float = 1f,
    dartAppear: Float = 1f,
) {
    Canvas(modifier) {
        val unit = size.minDimension / 108f
        val offX = (size.width - 108f * unit) / 2f
        val offY = (size.height - 108f * unit) / 2f
        fun pt(x: Float, y: Float) = Offset(offX + x * unit, offY + y * unit)
        val centre = pt(54f, 54f)

        // The mark settles into place by scaling up slightly while it fades in. There is no
        // badge behind it: the ring that used to be there read as fussy, and the page's own
        // ground does the job.
        val settle = MARK * (0.92f + 0.08f * markAppear)
        scale(settle, settle, centre) {
            scale(0.88f, 0.88f, pt(54f, 55f)) {
                tankard(::pt, unit, markAppear)
            }
        }

        // The dart flies in along its own diagonal, from outside the mark.
        val flyIn = 46f * (1f - dartAppear)
        scale(MARK, MARK, centre) {
            rotate(DART_AT, centre) {
                translate(flyIn * unit, 0f) {
                    dart(centre, unit, dartAppear)
                }
            }
        }
    }
}

/** The tankard: handle behind the body, two hoops across it, and the head on top. */
private fun DrawScope.tankard(pt: (Float, Float) -> Offset, unit: Float, alpha: Float) {
    val handle = Path().apply {
        moveTo(pt(64f, 44f))
        lineTo(pt(80f, 44f))
        lineTo(pt(80f, 72f))
        lineTo(pt(64f, 72f))
    }
    drawPath(handle, Gold, alpha = alpha, style = Stroke(8f * unit, join = StrokeJoin.Round))

    val body = Path().apply {
        moveTo(pt(39f, 33f))
        lineTo(pt(71f, 33f))
        quadraticTo(pt(76f, 58f), pt(71f, 86f))
        lineTo(pt(39f, 86f))
        quadraticTo(pt(34f, 58f), pt(39f, 33f))
        close()
    }
    drawPath(body, Gold, alpha = alpha)

    listOf(45f, 66f).forEach { y ->
        drawRect(Hoop, pt(35f, y), Size(40f * unit, 4.5f * unit), alpha = alpha)
    }

    // The head, as four curves off the rim.
    val foam = Path().apply {
        moveTo(pt(36.5f, 33f))
        cubicTo(pt(36.5f, 27.5f), pt(38.5f, 23f), pt(43f, 23f))
        cubicTo(pt(46f, 19.5f), pt(50f, 17.5f), pt(55f, 17.5f))
        cubicTo(pt(60f, 17.5f), pt(65f, 19.5f), pt(67f, 24f))
        cubicTo(pt(69.5f, 23f), pt(73.5f, 27.5f), pt(73.5f, 33f))
        close()
    }
    drawPath(foam, Foam, alpha = alpha)
}

/** The dart, lying along +x from [centre]: needle, barrel, grip rings, shaft, flight. */
private fun DrawScope.dart(centre: Offset, unit: Float, alpha: Float) {
    fun at(x: Float, y: Float) = Offset(centre.x + x * unit, centre.y + y * unit)
    fun box(x: Float, y: Float, w: Float, h: Float, r: Float = 0f, colour: Color = Barrel) =
        drawRoundRect(
            color = colour,
            topLeft = at(x, y),
            size = Size(w * unit, h * unit),
            cornerRadius = CornerRadius(r * unit),
            alpha = alpha,
        )

    val needle = Path().apply {
        moveTo(at(-38f, 0f))
        lineTo(at(-23f, -1.7f))
        lineTo(at(-23f, 1.7f))
        close()
    }
    drawPath(needle, Barrel, alpha = alpha)
    drawPath(needle, Accent, alpha = alpha, style = Stroke(1.2f * unit, join = StrokeJoin.Round))

    box(-24f, -3.9f, 27f, 7.8f, 3.4f)
    drawRoundRect(
        color = Accent,
        topLeft = at(-24f, -3.9f),
        size = Size(27f * unit, 7.8f * unit),
        cornerRadius = CornerRadius(3.4f * unit),
        alpha = alpha,
        style = Stroke(1.7f * unit),
    )
    GROOVES.forEach { x -> box(x, -3.6f, 1.6f, 7.2f, colour = Accent) }

    box(3f, -1.7f, 10f, 3.4f, 1.7f)
    drawRoundRect(
        color = Accent,
        topLeft = at(3f, -1.7f),
        size = Size(10f * unit, 3.4f * unit),
        cornerRadius = CornerRadius(1.7f * unit),
        alpha = alpha,
        style = Stroke(1.2f * unit),
    )

    val flight = Path().apply {
        moveTo(at(12f, 0f))
        lineTo(at(17.5f, -7.6f))
        lineTo(at(34f, -6.6f))
        lineTo(at(34f, 6.6f))
        lineTo(at(17.5f, 7.6f))
        close()
    }
    drawPath(flight, Barrel, alpha = alpha)
    drawPath(flight, Accent, alpha = alpha, style = Stroke(1.7f * unit, join = StrokeJoin.Round))
    drawLine(
        color = Accent,
        start = at(15f, 0f),
        end = at(33f, 0f),
        strokeWidth = 1.2f * unit,
        cap = StrokeCap.Round,
        alpha = 0.65f * alpha,
    )
}

private fun Path.moveTo(at: Offset) = moveTo(at.x, at.y)
private fun Path.lineTo(at: Offset) = lineTo(at.x, at.y)
private fun Path.quadraticTo(control: Offset, to: Offset) =
    quadraticTo(control.x, control.y, to.x, to.y)
private fun Path.cubicTo(c1: Offset, c2: Offset, to: Offset) =
    cubicTo(c1.x, c1.y, c2.x, c2.y, to.x, to.y)
