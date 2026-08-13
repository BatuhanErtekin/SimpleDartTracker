package com.batu.simpledarttracker.domain.cricket

import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.game.TurnOutcome
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.model.Ring
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CricketEngineTest {

    private val alice = Player("p1", "Alice")
    private val bob = Player("p2", "Bob")

    private val twenty = CricketTarget.Number(20)
    private val nineteen = CricketTarget.Number(19)
    private val house = CricketTarget.House

    private fun single(n: Int) = Dart.Segment(n, Ring.SINGLE)
    private fun double(n: Int) = Dart.Segment(n, Ring.DOUBLE)
    private fun triple(n: Int) = Dart.Segment(n, Ring.TRIPLE)

    private fun at(dart: Dart, target: CricketTarget) = CricketThrow(dart, target)
    private val miss = CricketThrow.missed()

    private fun game(
        targets: List<CricketTarget> = listOf(twenty, nineteen, CricketTarget.Bull),
        scoring: CricketScoring = CricketScoring.STANDARD,
        overrides: Map<CricketTarget, Int> = emptyMap(),
    ) = CricketEngine.newGame(CricketConfig(targets, overrides, scoring), listOf(alice, bob))

    private fun CricketGameState.stage(vararg throws: CricketThrow): CricketGameState =
        throws.fold(this) { state, t -> CricketEngine.throwDart(state, t) }

    private fun CricketGameState.confirm(): CricketGameState = CricketEngine.confirmTurn(this)

    /** A whole turn: the given throws, padded out with misses. */
    private fun CricketGameState.turn(vararg throws: CricketThrow): CricketGameState =
        stage(*throws, *Array(3 - throws.size) { miss }).confirm()

    @Test
    fun newGameStartsEverybodyEmpty() {
        val state = game()
        assertTrue(state.players.all { it.marks.isEmpty() && it.score == 0 })
        assertEquals(0, state.currentPlayerIndex)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
    }

    @Test
    fun throwsStageWithoutBeingApplied() {
        val state = game().stage(at(triple(20), twenty))
        assertEquals(1, state.currentThrows.size)
        assertEquals(0, state.currentPlayer.marksOn(twenty))
        assertEquals(TurnOutcome.ONGOING, CricketEngine.outcomeOf(state))
    }

    @Test
    fun aTurnIsOnlyAppliedOnceItIsComplete() {
        val staged = game().stage(at(triple(20), twenty))
        assertEquals(staged, staged.confirm())
    }

    @Test
    fun threeMarksCloseATarget() {
        val state = game().turn(at(triple(20), twenty))
        assertEquals(3, state.players[0].marksOn(twenty))
        assertTrue(state.hasClosed(state.players[0], twenty))
        assertEquals(1, state.currentPlayerIndex)
    }

    @Test
    fun marksNeverExceedWhatTheTargetNeeds() {
        val state = game().turn(at(triple(20), twenty), at(triple(20), twenty))
        assertEquals(3, state.players[0].marksOn(twenty))
    }

    @Test
    fun excessMarksBecomePointsForTheThrower() {
        // Six marks on a three-mark target: three close it, three spill over at 20 each.
        val state = game().turn(at(triple(20), twenty), at(triple(20), twenty))
        assertEquals(60, state.players[0].score)
        assertEquals(0, state.players[1].score)
    }

    @Test
    fun closingAndSpillingInOneDartScoresOnlyTheRemainder() {
        var state = game().turn(at(double(20), twenty)) // two marks, nothing closed yet
        assertEquals(0, state.players[0].score)
        state = state.turn() // Bob misses
        state = state.turn(at(triple(20), twenty)) // one mark closes, two spill over
        assertEquals(3, state.players[0].marksOn(twenty))
        assertEquals(40, state.players[0].score)
    }

    @Test
    fun aTargetEverybodyHasClosedPaysNothing() {
        var state = game().turn(at(triple(20), twenty)) // Alice closes 20
        state = state.turn(at(triple(20), twenty)) // Bob closes 20
        state = state.turn(at(triple(20), twenty)) // dead: nobody gains
        assertEquals(0, state.players[0].score)
        assertEquals(0, state.players[1].score)
    }

    @Test
    fun cutThroatGivesThePointsToOpponentsWhoAreStillOpen() {
        val state = game(scoring = CricketScoring.CUT_THROAT)
            .turn(at(triple(20), twenty), at(triple(20), twenty))
        assertEquals(0, state.players[0].score)
        assertEquals(60, state.players[1].score)
    }

    @Test
    fun ringTargetsTakeOneMarkAndPayTheWholeDart() {
        val doubles = CricketTarget.AnyDouble
        var state = game(targets = listOf(twenty, doubles), overrides = mapOf(doubles to 1))

        state = state.turn(at(double(5), doubles)) // closes Double, nothing to spill
        assertEquals(1, state.players[0].marksOn(doubles))
        assertEquals(0, state.players[0].score)

        state = state.turn() // Bob misses
        state = state.turn(at(double(20), doubles))
        assertEquals(40, state.players[0].score)
    }

    @Test
    fun throwsThatCannotServeTheirTargetAreRejected() {
        val state = game()
        assertEquals(state, CricketEngine.throwDart(state, at(triple(19), twenty)))
        assertEquals(state, CricketEngine.throwDart(state, at(Dart.Miss, twenty)))
        // 5 is not in play in this match.
        assertEquals(state, CricketEngine.throwDart(state, at(triple(5), CricketTarget.Number(5))))
    }

    @Test
    fun aDartWithNoTargetStillUsesUpASlot() {
        val state = game().stage(miss, miss)
        assertEquals(2, state.currentThrows.size)
        assertEquals(TurnOutcome.ONGOING, CricketEngine.outcomeOf(state))
        assertEquals(TurnOutcome.COMPLETE, CricketEngine.outcomeOf(state.stage(miss)))
    }

    @Test
    fun extraThrowsAreBlockedAfterThree() {
        val state = game().stage(at(single(20), twenty), miss, miss)
        assertEquals(TurnOutcome.COMPLETE, CricketEngine.outcomeOf(state))
        assertEquals(state, CricketEngine.throwDart(state, at(single(19), nineteen)))
    }

    @Test
    fun closingEverythingWithTheLeadWinsMidTurn() {
        val state = game(targets = listOf(twenty)).stage(at(triple(20), twenty))
        assertEquals(TurnOutcome.WIN, CricketEngine.outcomeOf(state))
        assertEquals(GameStatus.IN_PROGRESS, state.status) // not over until confirmed
        assertEquals(state, CricketEngine.throwDart(state, at(single(20), twenty))) // rest blocked

        val confirmed = state.confirm()
        assertEquals(GameStatus.FINISHED, confirmed.status)
        assertEquals("p1", confirmed.winnerId)
    }

    @Test
    fun closingEverythingWhileBehindOnPointsIsNotAWin() {
        var state = game(targets = listOf(twenty, nineteen))
        state = state.turn(at(triple(19), nineteen), at(triple(19), nineteen)) // Alice: 19 closed, 57
        state = state.turn(at(triple(20), twenty), at(triple(20), twenty)) // Bob: 20 closed, 60
        state = state.stage(at(triple(20), twenty)) // Alice closes 20 but trails 57–60

        assertEquals(TurnOutcome.ONGOING, CricketEngine.outcomeOf(state))
        assertEquals(57, state.players[0].score)
        assertEquals(60, state.players[1].score)
    }

    @Test
    fun aHouseIsNeverAwardedOnItsOwn() {
        // The same three darts entered one by one are just three darts.
        val state = game(targets = listOf(twenty, house))
            .stage(at(single(20), twenty), at(double(20), twenty), at(triple(20), twenty))
        assertFalse(state.houseClaimed)
        assertEquals(0, state.confirm().players[0].marksOn(house))
    }

    @Test
    fun claimingAHouseSpendsTheWholeTurnOnIt() {
        val state = CricketEngine.claimHouse(
            game(targets = listOf(twenty, nineteen, house)),
            listOf(single(20), double(20), triple(20)),
        )
        assertTrue(state.houseClaimed)
        assertEquals(3, state.currentThrows.size)

        val confirmed = state.confirm()
        assertEquals(1, confirmed.players[0].marksOn(house))
        // The darts paid for the house, so they do not mark 20 as well.
        assertEquals(0, confirmed.players[0].marksOn(twenty))
        assertEquals(0, confirmed.players[0].score)
    }

    @Test
    fun anUnclosedHouseNeverScores() {
        val state = CricketEngine.claimHouse(
            game(targets = listOf(twenty, house), scoring = CricketScoring.CUT_THROAT),
            listOf(single(20), double(20), triple(20)),
        ).confirm()
        assertEquals(1, state.players[0].marksOn(house)) // one of three
        assertTrue(state.players.all { it.score == 0 })
    }

    @Test
    fun aHouseReplacesWhateverWasAlreadyStaged() {
        val state = CricketEngine.claimHouse(
            game(targets = listOf(twenty, nineteen, house)).stage(at(single(19), nineteen)),
            listOf(single(20), double(20), triple(20)),
        )
        assertEquals(3, state.currentThrows.size)
        assertTrue(state.currentThrows.all { it.target == null })
    }

    @Test
    fun aHouseCannotBeClaimedFromDartsThatAreNotAllOnOneTarget() {
        val state = game(targets = listOf(twenty, nineteen, house))
        assertEquals(state, CricketEngine.claimHouse(state, listOf(single(20), single(20), single(19))))
        assertEquals(state, CricketEngine.claimHouse(state, listOf(single(20), single(20))))
    }

    @Test
    fun aHouseCannotBeClaimedWhenTheMatchIsNotPlayingIt() {
        val state = game(targets = listOf(twenty, nineteen))
        assertEquals(state, CricketEngine.claimHouse(state, listOf(single(20), double(20), triple(20))))
    }

    @Test
    fun aClosedHouseScoresTheWholeTurn() {
        // House closes on one mark here, so the second house spills straight into points.
        var state = game(targets = listOf(nineteen, house), overrides = mapOf(house to 1))
        val houseDarts = listOf(single(20), double(20), triple(20))
        // 20 is not even in play here, but that no longer matters: a house never marks a bed.

        state = CricketEngine.claimHouse(state, houseDarts).confirm()
        assertEquals(1, state.players[0].marksOn(house))
        assertEquals(0, state.players[0].score)

        state = state.turn() // Bob misses
        state = CricketEngine.claimHouse(state, houseDarts).confirm()
        assertEquals(20 + 40 + 60, state.players[0].score)
    }

    @Test
    fun aMarkOnlyNeedsItsNumberOnceItCanScore() {
        val doubles = CricketTarget.AnyDouble
        var state = game(targets = listOf(twenty, doubles), overrides = mapOf(doubles to 1))

        // Nothing to spill yet, so the number behind the mark changes nothing.
        assertFalse(CricketEngine.wouldScore(state, doubles))

        // Staged, not yet confirmed: the answer keeps up within the turn.
        val closing = state.stage(at(double(5), doubles))
        assertTrue(CricketEngine.wouldScore(closing, doubles))

        state = closing.turn() // pad the turn out and confirm
        state = state.turn() // Bob misses
        assertTrue(CricketEngine.wouldScore(state, doubles))
    }

    @Test
    fun aDeadTargetNeverNeedsItsNumber() {
        var state = game(targets = listOf(twenty, nineteen))
        state = state.turn(at(triple(20), twenty)) // Alice closes 20
        assertTrue(CricketEngine.wouldScore(state.turn(), twenty)) // Bob passes; still open for him
        state = state.turn(at(triple(20), twenty)) // Bob closes 20 too
        assertFalse(CricketEngine.wouldScore(state, twenty))
    }

    @Test
    fun aHouseOnlyNeedsItsDartsOnceItCanScore() {
        val targets = listOf(twenty, house)
        var state = game(targets = targets, overrides = mapOf(house to 1))
        assertFalse(CricketEngine.wouldScore(state, house))

        state = CricketEngine.claimHouse(state, listOf(single(20), double(20), triple(20))).confirm()
        state = state.turn() // Bob misses, so House is still open for him
        assertTrue(CricketEngine.wouldScore(state, house))
    }

    @Test
    fun aTargetOutOfPlayNeverNeedsItsNumber() {
        assertFalse(CricketEngine.wouldScore(game(), CricketTarget.House))
    }

    @Test
    fun thePreviewShowsTheStagedTurnWithoutApplyingIt() {
        val staged = game().stage(at(triple(20), twenty), at(triple(20), twenty))
        assertEquals(0, staged.players[0].marksOn(twenty)) // untouched
        assertEquals(0, staged.players[0].score)

        val preview = CricketEngine.preview(staged)
        assertEquals(3, preview.players[0].marksOn(twenty))
        assertEquals(60, preview.players[0].score)
        // The preview is a picture, not a move: the turn is still on the board.
        assertEquals(2, preview.currentThrows.size)
        assertEquals(0, preview.currentPlayerIndex)
    }

    @Test
    fun handingTheThrowOnKeepsWhatTheLastPlayerEntered() {
        // Alice records one dart, then Bob steps up: her short visit still counts.
        val state = game().stage(at(triple(20), twenty))
        val passed = CricketEngine.passTurnTo(state, 1)

        assertEquals(1, passed.currentPlayerIndex)
        assertTrue(passed.currentThrows.isEmpty())
        assertEquals(3, passed.players[0].marksOn(twenty))
        assertEquals(0, passed.players[1].marksOn(twenty))
    }

    @Test
    fun handingTheThrowOnClearsAHouseClaim() {
        val claimed = CricketEngine.claimHouse(
            game(targets = listOf(twenty, house)),
            listOf(single(20), double(20), triple(20)),
        )
        val passed = CricketEngine.passTurnTo(claimed, 1)
        assertEquals(1, passed.players[0].marksOn(house)) // the house still landed
        assertFalse(passed.houseClaimed)
        assertEquals(1, passed.currentPlayerIndex)
    }

    @Test
    fun handingTheThrowToNobodyInParticularIsIgnored() {
        val state = game().stage(at(triple(20), twenty))
        assertEquals(state, CricketEngine.passTurnTo(state, 0)) // already theirs
        assertEquals(state, CricketEngine.passTurnTo(state, -1))
        assertEquals(state, CricketEngine.passTurnTo(state, 5))
    }

    @Test
    fun aWonTurnCannotBeTakenAway() {
        val won = game(targets = listOf(twenty)).stage(at(triple(20), twenty))
        val passed = CricketEngine.passTurnTo(won, 1)
        assertEquals(GameStatus.FINISHED, passed.status)
        assertEquals("p1", passed.winnerId)
    }

    @Test
    fun wouldScoreCanBeAskedAboutAnyPlayer() {
        var state = game(targets = listOf(twenty, nineteen))
        state = state.turn(at(triple(20), twenty)) // Alice closes 20, Bob to throw
        assertTrue(CricketEngine.wouldScore(state, twenty, playerIndex = 0))
        assertFalse(CricketEngine.wouldScore(state, twenty, playerIndex = 1))
    }

    @Test
    fun actionsAfterTheGameEndsAreIgnored() {
        val finished = game(targets = listOf(twenty)).stage(at(triple(20), twenty)).confirm()
        assertEquals(GameStatus.FINISHED, finished.status)
        assertEquals(finished, CricketEngine.throwDart(finished, at(triple(20), twenty)))
        assertEquals(finished, CricketEngine.confirmTurn(finished))
    }

    @Test
    fun movingAPlayerKeepsTheThrowWithTheSamePerson() {
        val state = game().turn(at(single(20), twenty)) // Bob to throw
        val moved = CricketEngine.movePlayer(state, fromIndex = 1, toIndex = 0)
        assertEquals(listOf(bob, alice), moved.players.map { it.player })
        assertEquals(bob, moved.currentPlayer.player)
    }
}
