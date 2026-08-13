package com.batu.simpledarttracker.domain.cricket

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CricketTargetTest {

    @Test
    fun standardSetIs15To20AndTheBull() {
        assertEquals(7, StandardCricketTargets.size)
        (15..20).forEach { assertTrue(CricketTarget.Number(it) in StandardCricketTargets) }
        assertTrue(CricketTarget.Bull in StandardCricketTargets)
    }

    @Test
    fun standardSetExcludesTheRingsAndHouse() {
        assertTrue(CricketTarget.AnyDouble !in StandardCricketTargets)
        assertTrue(CricketTarget.AnyTriple !in StandardCricketTargets)
        assertTrue(CricketTarget.House !in StandardCricketTargets)
    }

    @Test
    fun randomSetIsTheBullPlusFiveDistinctNumbers() {
        repeat(20) { seed ->
            val targets = randomCricketTargets(Random(seed))
            assertTrue(CricketTarget.Bull in targets)
            val numbers = targets.filterIsInstance<CricketTarget.Number>()
            assertEquals(RANDOM_TARGET_COUNT, numbers.size)
            assertEquals(numbers.size, numbers.distinct().size)
            assertTrue(numbers.all { it.value in 1..20 })
        }
    }

    @Test
    fun randomSetIsReproducibleForASeed() {
        assertEquals(randomCricketTargets(Random(7)), randomCricketTargets(Random(7)))
    }

    @Test
    fun idsRoundTrip() {
        val all = CricketTarget.numbers + CricketTarget.extras
        all.forEach { assertEquals(it, CricketTarget.ofId(it.id)) }
    }

    @Test
    fun unknownIdsAreRejected() {
        assertNull(CricketTarget.ofId("21"))
        assertNull(CricketTarget.ofId("0"))
        assertNull(CricketTarget.ofId("bullseye"))
        assertNull(CricketTarget.ofId(""))
    }

    @Test
    fun marksDefaultToThreeAndOnlyChangeWhereOverridden() {
        val overrides = mapOf<CricketTarget, Int>(CricketTarget.House to 1, CricketTarget.Bull to 2)
        assertEquals(1, marksToClose(CricketTarget.House, overrides))
        assertEquals(2, marksToClose(CricketTarget.Bull, overrides))
        assertEquals(DEFAULT_MARKS_TO_CLOSE, marksToClose(CricketTarget.AnyDouble, overrides))
        assertEquals(DEFAULT_MARKS_TO_CLOSE, marksToClose(CricketTarget.Number(20), overrides))
        assertEquals(DEFAULT_MARKS_TO_CLOSE, marksToClose(CricketTarget.Bull, emptyMap()))
    }

    @Test
    fun markChoicesAreOneToThreeWithThreeAsTheDefault() {
        assertEquals(listOf(1, 2, 3), MarkChoices)
        assertTrue(DEFAULT_MARKS_TO_CLOSE in MarkChoices)
        assertEquals(3, DEFAULT_MARKS_TO_CLOSE)
    }

    @Test
    fun numbersAreListedHighestFirst() {
        assertEquals(20, CricketTarget.numbers.first().value)
        assertEquals(1, CricketTarget.numbers.last().value)
        assertEquals(20, CricketTarget.numbers.size)
    }
}
