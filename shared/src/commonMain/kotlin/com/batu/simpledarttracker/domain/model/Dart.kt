package com.batu.simpledarttracker.domain.model

/** A single dart throw and its contribution to the score. */
sealed interface Dart {
    /** Points this dart scores. */
    val value: Int

    /** Whether this dart counts as a "double" for a double-out checkout (double bull included). */
    val isDouble: Boolean

    /**
     * Target used to detect a "house" (all three darts of a turn on the same target).
     * Outer and inner bull share one bull target; a miss has no target.
     */
    val houseTarget: Int?

    /** A numbered bed 1..20 in one of the three rings. */
    data class Segment(val number: Int, val ring: Ring) : Dart {
        init { require(number in 1..20) { "Segment number must be 1..20, was $number" } }
        override val value: Int get() = number * ring.factor
        override val isDouble: Boolean get() = ring == Ring.DOUBLE
        override val houseTarget: Int get() = number
    }

    /** Outer bull, 25 points. */
    data object Bull : Dart {
        override val value = 25
        override val isDouble = false
        override val houseTarget = BULL_TARGET
    }

    /** Inner bull / "redbull", 50 points. Counts as a double for checkout. */
    data object DoubleBull : Dart {
        override val value = 50
        override val isDouble = true
        override val houseTarget = BULL_TARGET
    }

    /** Off-target throw, 0 points. */
    data object Miss : Dart {
        override val value = 0
        override val isDouble = false
        override val houseTarget = null
    }

    companion object {
        /** Shared house target for both outer and inner bull. */
        const val BULL_TARGET = 25
    }
}

/** The ring a numbered dart lands in, with its score multiplier. */
enum class Ring(val factor: Int) { SINGLE(1), DOUBLE(2), TRIPLE(3) }
