package com.batu.simpledarttracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val Pill = RoundedCornerShape(percent = 50)
private val PadInside = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

/**
 * The app's own filled button: a gold pill with dark type, and the same press the keys have.
 *
 * It is not a Material button with different colours — Material's ripple washes over the whole
 * surface, and next to a pad of keys that dip under the finger, that reads as two different
 * apps.
 */
@Composable
fun BrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = Brand.Gold,
    content: Color = Brand.Ink,
    // A lit pill flares in its own colour, brightened; a dark one flares gold.
    flare: Color = if (container == Brand.Gold) Brand.Honey else Brand.Gold,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .pressable(onClick = onClick, enabled = enabled, flare = flare)
            .background(if (enabled) container else container.copy(alpha = 0.35f), Pill)
            .padding(PadInside),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) content else content.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

/** The quieter one: an outline in [content], for the choice that is not the main one. */
@Composable
fun BrandOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: Color = Brand.Gold,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .pressable(onClick = onClick, enabled = enabled, flare = content)
            .border(1.5.dp, if (enabled) content.copy(alpha = 0.55f) else Brand.Edge, Pill)
            .padding(PadInside),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) content else Brand.Wire,
            textAlign = TextAlign.Center,
        )
    }
}
