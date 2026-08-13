package com.batu.simpledarttracker.domain.cricket

import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Ring

/**
 * A dart together with the target the player is claiming it for.
 *
 * One dart can serve more than one target — D20 is worth two marks on 20 or one on Double — and
 * which it counts for is the player's call, not the engine's. So the board asks, and the answer
 * travels with the dart.
 *
 * A null [target] is a dart that scored nothing: a miss, or a bed this match is not playing.
 * It still takes one of the three slots in the turn.
 */
data class CricketThrow(val dart: Dart, val target: CricketTarget?) {
    companion object {
        /** A dart that did not land on anything in play. */
        fun missed(dart: Dart = Dart.Miss) = CricketThrow(dart, null)
    }
}

/**
 * Marks [dart] puts on [target] when claimed there, or zero if the dart cannot serve it.
 *
 * A numbered bed pays its ring: a single is one mark, a double two, a triple three. The ring
 * targets pay one mark per dart however big the ring is, since the achievement is hitting *a*
 * double, not that particular one. The inner bull is two marks on Bull and, being a double,
 * one mark on Double.
 *
 * [CricketTarget.House] never scores from a single dart — a house is a whole turn on one
 * target — so it always returns zero here.
 */
fun marksFor(dart: Dart, target: CricketTarget): Int = when (target) {
    is CricketTarget.Number ->
        if (dart is Dart.Segment && dart.number == target.value) dart.ring.factor else 0

    CricketTarget.Bull -> when (dart) {
        Dart.Bull -> 1
        Dart.DoubleBull -> 2
        else -> 0
    }

    CricketTarget.AnyDouble -> if (dart.isDouble) 1 else 0

    CricketTarget.AnyTriple -> if (dart is Dart.Segment && dart.ring == Ring.TRIPLE) 1 else 0

    CricketTarget.House -> 0
}

/**
 * What one mark on [target] is worth in points, for a dart that produced it.
 *
 * Scoring in Cricket is per mark, not per dart, because a throw can both close a target and
 * spill over: with two marks already on 20, a treble closes it and the third mark scores 20.
 * Dividing the dart's face value by the marks it carries gives that per-mark value — 20 for a
 * numbered bed, 25 for the bull, and the dart's whole value for the ring targets, which only
 * ever pay one mark.
 */
fun pointsPerMark(dart: Dart, target: CricketTarget): Int {
    val marks = marksFor(dart, target)
    return if (marks == 0) 0 else dart.value / marks
}
