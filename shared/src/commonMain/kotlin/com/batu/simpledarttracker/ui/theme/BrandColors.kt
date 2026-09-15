package com.batu.simpledarttracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The app's colours, taken from the logo: a tankard of amber on a warm near-black.
 *
 * The three game colours are three steps of the same warm family rather than three separate hues
 * — saturated gold, pale honey, and an ember that has turned towards red — so the app reads as
 * one thing rather than a colour wheel. They stay far enough apart in lightness to tell the
 * modes apart at a glance, which is the only job they actually have.
 *
 * Nothing here is flat black. [Night] is a brown-black, and the page under it is painted with a
 * gradient (see Backdrop.kt): a true #000 ground makes the amber look like it is floating.
 */
object Brand {
    val Night = Color(0xFF100D08)   // page
    val Panel = Color(0xFF1A150E)   // surface: panels, dialogs
    val Key = Color(0xFF262017)     // raised: cards, keys, cells
    val Edge = Color(0xFF332A1D)    // hairlines and outlines
    val Board = Color(0xFF171208)   // the dart board's own black

    val Chalk = Color(0xFFF5EFE2)   // type on the dark ground
    val Wire = Color(0xFFA2957E)    // muted type and hairlines
    val Ink = Color(0xFF17110A)     // type on a filled accent

    val Gold = Color(0xFFF0A828)    // primary · X01 · the logo's own amber
    val Ember = Color(0xFFD95A2B)   // secondary · Cricket
    val Honey = Color(0xFFFFDE9B)   // tertiary · Training

    // Deep versions, for large fills that still carry cream type.
    val GoldDeep = Color(0xFF5B3A0B)
    val EmberDeep = Color(0xFF5A2412)
    val HoneyDeep = Color(0xFF5E4A1C)
}
