package com.batu.simpledarttracker.domain.x01

/** Rules for an X01 game (e.g. 501 / 301). */
data class X01Config(
    val startingScore: Int = 501,
    val doubleOut: Boolean = true,
)
