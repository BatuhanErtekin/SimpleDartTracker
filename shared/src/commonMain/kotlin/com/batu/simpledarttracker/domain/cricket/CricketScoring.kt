package com.batu.simpledarttracker.domain.cricket

/** How points behave once a player has closed a target. */
enum class CricketScoring {
    /**
     * The usual game: hitting a target you have closed scores for you, for as long as at least
     * one opponent still has it open. Highest score wins.
     */
    STANDARD,

    /**
     * "Cut-throat": the same hits hand those points to every opponent who has *not* closed the
     * target, so scoring is a punishment rather than a reward. Lowest score wins.
     */
    CUT_THROAT,
}
