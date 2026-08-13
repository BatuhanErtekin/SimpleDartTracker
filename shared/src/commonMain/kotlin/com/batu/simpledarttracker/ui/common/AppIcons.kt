package com.batu.simpledarttracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** Hamburger (three-line) menu icon — drawn on a Canvas so no icon dependency is needed. */
@Composable
fun HamburgerIcon(
    color: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.withDescription(contentDescription)) {
        val strokeW = size.height * 0.09f
        listOf(0.28f, 0.5f, 0.72f).forEach { fy ->
            drawLine(
                color = color,
                start = Offset(size.width * 0.16f, size.height * fy),
                end = Offset(size.width * 0.84f, size.height * fy),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** The classic vertical three-dot overflow icon. */
@Composable
fun MoreIcon(
    color: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.withDescription(contentDescription)) {
        val radius = size.minDimension * 0.085f
        listOf(0.22f, 0.5f, 0.78f).forEach { fy ->
            drawCircle(color, radius = radius, center = Offset(size.width / 2f, size.height * fy))
        }
    }
}

/** Back (chevron) icon. Rotate it a quarter turn to point up or down. */
@Composable
fun BackIcon(
    color: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.withDescription(contentDescription)) {
        val strokeW = size.height * 0.09f
        val x = size.width
        val y = size.height
        drawLine(color, Offset(x * 0.62f, y * 0.22f), Offset(x * 0.38f, y * 0.5f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(color, Offset(x * 0.38f, y * 0.5f), Offset(x * 0.62f, y * 0.78f), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

private fun Modifier.withDescription(description: String?): Modifier =
    if (description != null) this.semantics { contentDescription = description } else this
