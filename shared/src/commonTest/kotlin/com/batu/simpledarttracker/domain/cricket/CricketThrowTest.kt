package com.batu.simpledarttracker.domain.cricket

import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Ring
import kotlin.test.Test
import kotlin.test.assertEquals

class CricketThrowTest {

    private fun single(n: Int) = Dart.Segment(n, Ring.SINGLE)
    private fun double(n: Int) = Dart.Segment(n, Ring.DOUBLE)
    private fun triple(n: Int) = Dart.Segment(n, Ring.TRIPLE)

    @Test
    fun numberedBedPaysItsRing() {
        val twenty = CricketTarget.Number(20)
        assertEquals(1, marksFor(single(20), twenty))
        assertEquals(2, marksFor(double(20), twenty))
        assertEquals(3, marksFor(triple(20), twenty))
    }

    @Test
    fun anotherNumberScoresNothing() {
        assertEquals(0, marksFor(triple(19), CricketTarget.Number(20)))
        assertEquals(0, marksFor(Dart.Bull, CricketTarget.Number(20)))
        assertEquals(0, marksFor(Dart.Miss, CricketTarget.Number(20)))
    }

    @Test
    fun bullRingsPayOneAndTwo() {
        assertEquals(1, marksFor(Dart.Bull, CricketTarget.Bull))
        assertEquals(2, marksFor(Dart.DoubleBull, CricketTarget.Bull))
        assertEquals(0, marksFor(triple(20), CricketTarget.Bull))
    }

    @Test
    fun ringTargetsPayOneMarkWhateverTheNumber() {
        assertEquals(1, marksFor(double(1), CricketTarget.AnyDouble))
        assertEquals(1, marksFor(double(20), CricketTarget.AnyDouble))
        assertEquals(1, marksFor(triple(3), CricketTarget.AnyTriple))
        assertEquals(1, marksFor(triple(20), CricketTarget.AnyTriple))
    }

    @Test
    fun theInnerBullCountsAsADoubleButNotAsATriple() {
        assertEquals(1, marksFor(Dart.DoubleBull, CricketTarget.AnyDouble))
        assertEquals(0, marksFor(Dart.Bull, CricketTarget.AnyDouble))
        assertEquals(0, marksFor(Dart.DoubleBull, CricketTarget.AnyTriple))
    }

    @Test
    fun ringTargetsRejectTheWrongRing() {
        assertEquals(0, marksFor(single(20), CricketTarget.AnyDouble))
        assertEquals(0, marksFor(triple(20), CricketTarget.AnyDouble))
        assertEquals(0, marksFor(double(20), CricketTarget.AnyTriple))
        assertEquals(0, marksFor(Dart.Miss, CricketTarget.AnyDouble))
    }

    @Test
    fun houseNeverComesFromASingleDart() {
        assertEquals(0, marksFor(triple(20), CricketTarget.House))
        assertEquals(0, marksFor(Dart.DoubleBull, CricketTarget.House))
    }

    @Test
    fun aMarkOnANumberIsWorthThatNumber() {
        val twenty = CricketTarget.Number(20)
        assertEquals(20, pointsPerMark(single(20), twenty))
        assertEquals(20, pointsPerMark(double(20), twenty))
        assertEquals(20, pointsPerMark(triple(20), twenty))
    }

    @Test
    fun aMarkOnTheBullIsWorth25FromEitherRing() {
        assertEquals(25, pointsPerMark(Dart.Bull, CricketTarget.Bull))
        assertEquals(25, pointsPerMark(Dart.DoubleBull, CricketTarget.Bull))
    }

    @Test
    fun ringTargetsPayTheWholeDartBecauseTheyOnlyEverEarnOneMark() {
        assertEquals(40, pointsPerMark(double(20), CricketTarget.AnyDouble))
        assertEquals(60, pointsPerMark(triple(20), CricketTarget.AnyTriple))
        assertEquals(50, pointsPerMark(Dart.DoubleBull, CricketTarget.AnyDouble))
    }

    @Test
    fun aDartThatCannotServeItsTargetScoresNothing() {
        assertEquals(0, pointsPerMark(triple(19), CricketTarget.Number(20)))
        assertEquals(0, pointsPerMark(single(20), CricketTarget.AnyDouble))
        assertEquals(0, pointsPerMark(triple(20), CricketTarget.House))
    }
}
