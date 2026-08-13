package com.batu.simpledarttracker.domain.cricket

import com.batu.simpledarttracker.domain.model.Dart
import kotlin.random.Random

/**
 * Something a Cricket player can close. Beyond the numbered beds and the bull, a match can put
 * the rings themselves in play — the "Tactics" variant treats doubles and triples as their own
 * objectives — and a house, meaning all three darts of one turn on the same target.
 *
 * [id] is a stable key: it survives a rotation and, later, a database round trip.
 */
sealed interface CricketTarget {
    val id: String

    /** A numbered bed, 1..20. */
    data class Number(val value: Int) : CricketTarget {
        init { require(value in 1..20) { "Cricket number must be 1..20, was $value" } }
        override val id: String get() = value.toString()
    }

    /** Either bull ring. */
    data object Bull : CricketTarget {
        override val id: String = "bull"
    }

    /** Any double, whatever the number. */
    data object AnyDouble : CricketTarget {
        override val id: String = "double"
    }

    /** Any triple, whatever the number. */
    data object AnyTriple : CricketTarget {
        override val id: String = "triple"
    }

    /** All three darts of a turn on one target. */
    data object House : CricketTarget {
        override val id: String = "house"
    }

    companion object {
        /** Every numbered bed, highest first — the order the picker lays them out in. */
        val numbers: List<Number> = (20 downTo 1).map(::Number)

        /** Everything that is not a numbered bed, in picker order. */
        val extras: List<CricketTarget> = listOf(Bull, AnyDouble, AnyTriple, House)

        /** The target a dart physically landed on. Null for a miss, which lands nowhere. */
        fun ofDart(dart: Dart): CricketTarget? = when (val landed = dart.houseTarget) {
            null -> null
            Dart.BULL_TARGET -> Bull
            else -> Number(landed)
        }

        fun ofId(id: String): CricketTarget? = when (id) {
            Bull.id -> Bull
            AnyDouble.id -> AnyDouble
            AnyTriple.id -> AnyTriple
            House.id -> House
            else -> id.toIntOrNull()?.takeIf { it in 1..20 }?.let(::Number)
        }
    }
}

/** How many numbers a random match draws alongside the bull. */
const val RANDOM_TARGET_COUNT = 5

/** The classic set: 15 through 20, plus the bull. */
val StandardCricketTargets: Set<CricketTarget> =
    ((15..20).map(CricketTarget::Number) + CricketTarget.Bull).toSet()

/**
 * The bull plus [RANDOM_TARGET_COUNT] numbers drawn from the whole board, so a match can land
 * on beds nobody practises.
 */
fun randomCricketTargets(random: Random = Random.Default): Set<CricketTarget> =
    ((1..20).shuffled(random).take(RANDOM_TARGET_COUNT).map(CricketTarget::Number) + CricketTarget.Bull)
        .toSet()
