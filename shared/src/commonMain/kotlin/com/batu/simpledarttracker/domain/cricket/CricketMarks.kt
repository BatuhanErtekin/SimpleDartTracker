package com.batu.simpledarttracker.domain.cricket

/** Marks needed to close a target unless the match says otherwise. */
const val DEFAULT_MARKS_TO_CLOSE = 3

/** The mark requirements a target can be set to. */
val MarkChoices: List<Int> = listOf(1, 2, 3)

/**
 * How many marks close [target] in this match. Only the non-numbered targets
 * ([CricketTarget.extras]) can be tuned — a numbered bed always takes three, as in the paper
 * game. Anything absent from [overrides] takes [DEFAULT_MARKS_TO_CLOSE].
 */
fun marksToClose(target: CricketTarget, overrides: Map<CricketTarget, Int>): Int =
    overrides[target] ?: DEFAULT_MARKS_TO_CLOSE
