package com.batu.simpledarttracker.domain.model

/** Rules for an X01 game (e.g. 501 / 301). */
data class GameConfig(
    val startingScore: Int = 501,
    val doubleOut: Boolean = true,
)

/** Lifecycle state of a game. */
enum class GameStatus { IN_PROGRESS, FINISHED, ABANDONED }
