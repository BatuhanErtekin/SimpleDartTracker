package com.batu.simpledarttracker.ui.setup

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.batu.simpledarttracker.domain.game.GameMode
import com.batu.simpledarttracker.ui.theme.Brand

/**
 * The colour a mode brings with it. [vivid] is for small, bright marks — a caption, an active
 * segment — and [deep] fills whole cards, where a bright ground would fight the cream type.
 */
data class ModeAccent(val vivid: Color, val deep: Color)

/** Green for X01, red for Cricket, turquoise for Training: the same three the wheel uses. */
fun accentFor(mode: GameMode): ModeAccent = when (mode) {
    GameMode.X01 -> ModeAccent(Brand.Gold, Brand.GoldDeep)
    GameMode.CRICKET -> ModeAccent(Brand.Ember, Brand.EmberDeep)
    GameMode.TRAINING -> ModeAccent(Brand.Honey, Brand.HoneyDeep)
}

/** The small all-caps heading above each group of options. */
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Card colours for a selectable tile, filled with [accent] once chosen. */
@Composable
fun setupTileColors(selected: Boolean, accent: ModeAccent): CardColors = CardDefaults.cardColors(
    containerColor = if (selected) accent.deep else MaterialTheme.colorScheme.surfaceVariant,
)
