package com.batu.simpledarttracker.domain.model

/**
 * One visit to the board: up to three darts thrown by a single player.
 * A turn that busts scores zero for the visit.
 */
data class Turn(
    val playerId: String,
    val darts: List<Dart> = emptyList(),
    val isBust: Boolean = false,
) {
    init { require(darts.size <= 3) { "A turn has at most 3 darts, was ${darts.size}" } }

    /** Points credited for this turn (zero on a bust). */
    val scored: Int get() = if (isBust) 0 else darts.sumOf { it.value }

    /** True when all three darts landed on the same target (a "house"). */
    val isHouse: Boolean get() = isHouse(darts)
}

/**
 * Whether [darts] form a house: three darts on one target. The ring does not matter — 20, D20
 * and T20 are all the same target — but a miss has no target and so breaks it.
 */
fun isHouse(darts: List<Dart>): Boolean {
    if (darts.size != 3) return false
    val targets = darts.map { it.houseTarget }
    return targets.none { it == null } && targets.toSet().size == 1
}
