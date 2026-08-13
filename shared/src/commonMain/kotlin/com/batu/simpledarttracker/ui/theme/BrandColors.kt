package com.batu.simpledarttracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The app's colours. Three vivid accents — green, red and amber — over a near-black ground.
 *
 * The accents are bright enough that cream text on them would be hard to read, so anything
 * sitting on a filled accent uses [Ink] instead. That pairing, saturated fill with dark type,
 * is what keeps the palette looking current rather than merely colourful.
 */
object Brand {
    val Slate = Color(0xFF0D1210)   // page
    val Slate2 = Color(0xFF161C19)  // surface: panels, dialogs
    val Key = Color(0xFF212926)     // raised: cards, keys, cells

    val Chalk = Color(0xFFF1EEE6)   // type on the dark ground
    val Wire = Color(0xFF8C948F)    // muted type and hairlines
    val Ink = Color(0xFF08130D)     // type on a filled accent

    val Green = Color(0xFF22C55E)   // primary · X01
    val Red = Color(0xFFEF4444)     // secondary · Cricket
    val Amber = Color(0xFFF5A524)   // tertiary · Training

    // Deep versions, for large fills that still carry cream type.
    val GreenDeep = Color(0xFF14532D)
    val RedDeep = Color(0xFF5C1A1A)
    val AmberDeep = Color(0xFF5A3A08)
}
