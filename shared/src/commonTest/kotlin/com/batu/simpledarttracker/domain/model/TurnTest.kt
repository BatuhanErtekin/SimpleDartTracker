package com.batu.simpledarttracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TurnTest {

    @Test
    fun segmentValuesUseRingMultiplier() {
        assertEquals(20, Dart.Segment(20, Ring.SINGLE).value)
        assertEquals(40, Dart.Segment(20, Ring.DOUBLE).value)
        assertEquals(60, Dart.Segment(20, Ring.TRIPLE).value)
    }

    @Test
    fun bullValues() {
        assertEquals(25, Dart.Bull.value)
        assertEquals(50, Dart.DoubleBull.value)
        assertEquals(0, Dart.Miss.value)
    }

    @Test
    fun doubleBullCountsAsDoubleForCheckout() {
        assertTrue(Dart.DoubleBull.isDouble)
        assertTrue(Dart.Segment(20, Ring.DOUBLE).isDouble)
        assertFalse(Dart.Bull.isDouble)
        assertFalse(Dart.Segment(20, Ring.TRIPLE).isDouble)
    }

    @Test
    fun turnScoresSumOfDarts() {
        val turn = Turn("p1", listOf(Dart.Segment(20, Ring.TRIPLE), Dart.Segment(19, Ring.TRIPLE), Dart.Bull))
        assertEquals(60 + 57 + 25, turn.scored)
    }

    @Test
    fun bustScoresZero() {
        val turn = Turn("p1", listOf(Dart.Segment(20, Ring.TRIPLE)), isBust = true)
        assertEquals(0, turn.scored)
    }

    @Test
    fun sameNumberDifferentRingsIsHouse() {
        val turn = Turn("p1", listOf(
            Dart.Segment(20, Ring.SINGLE),
            Dart.Segment(20, Ring.DOUBLE),
            Dart.Segment(20, Ring.TRIPLE),
        ))
        assertTrue(turn.isHouse)
    }

    @Test
    fun threeBullsIsHouse() {
        val turn = Turn("p1", listOf(Dart.Bull, Dart.DoubleBull, Dart.Bull))
        assertTrue(turn.isHouse)
    }

    @Test
    fun differentNumbersIsNotHouse() {
        val turn = Turn("p1", listOf(
            Dart.Segment(20, Ring.TRIPLE),
            Dart.Segment(19, Ring.TRIPLE),
            Dart.Segment(20, Ring.SINGLE),
        ))
        assertFalse(turn.isHouse)
    }

    @Test
    fun missBreaksHouse() {
        val turn = Turn("p1", listOf(
            Dart.Segment(20, Ring.SINGLE),
            Dart.Segment(20, Ring.TRIPLE),
            Dart.Miss,
        ))
        assertFalse(turn.isHouse)
    }

    @Test
    fun fewerThanThreeDartsIsNotHouse() {
        val turn = Turn("p1", listOf(Dart.Segment(20, Ring.SINGLE), Dart.Segment(20, Ring.TRIPLE)))
        assertFalse(turn.isHouse)
    }
}
