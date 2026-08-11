package com.batu.simpledarttracker.domain.engine

import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.GameConfig
import com.batu.simpledarttracker.domain.model.GameStatus
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.model.Ring
import com.batu.simpledarttracker.domain.model.TurnOutcome
import com.batu.simpledarttracker.domain.model.X01GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class X01EngineTest {

    private val alice = Player("p1", "Alice")
    private val bob = Player("p2", "Bob")

    private fun game(startingScore: Int, doubleOut: Boolean = true) =
        X01Engine.newGame(GameConfig(startingScore, doubleOut), listOf(alice, bob))

    private fun X01GameState.stage(vararg darts: Dart): X01GameState =
        darts.fold(this) { s, d -> X01Engine.throwDart(s, d) }

    private fun X01GameState.confirm(): X01GameState = X01Engine.confirmTurn(this)

    private fun single(n: Int) = Dart.Segment(n, Ring.SINGLE)
    private fun double(n: Int) = Dart.Segment(n, Ring.DOUBLE)
    private fun triple(n: Int) = Dart.Segment(n, Ring.TRIPLE)

    @Test
    fun newGameStartsEveryoneAtStartingScore() {
        val state = game(501)
        assertEquals(2, state.players.size)
        assertTrue(state.players.all { it.remaining == 501 })
        assertEquals(0, state.currentPlayerIndex)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
    }

    @Test
    fun dartsStageWithoutAdvancing() {
        val state = game(501).stage(triple(20), triple(20))
        assertEquals(0, state.currentPlayerIndex)
        assertEquals(2, state.currentDarts.size)
        assertEquals(501 - 120, state.currentRemaining)
        assertEquals(501, state.currentPlayer.remaining) // not applied yet
        assertEquals(TurnOutcome.ONGOING, X01Engine.outcomeOf(state))
    }

    @Test
    fun threeDartsMarkCompleteButDoNotAdvance() {
        val state = game(501).stage(triple(20), triple(19), single(20)) // 3 darts
        assertEquals(TurnOutcome.COMPLETE, X01Engine.outcomeOf(state))
        assertEquals(0, state.currentPlayerIndex) // waiting for confirmation
    }

    @Test
    fun extraDartsBlockedAfterThree() {
        val state = game(501).stage(single(1), single(1), single(1))
        val blocked = X01Engine.throwDart(state, triple(20))
        assertEquals(state, blocked)
    }

    @Test
    fun confirmCompletedTurnAdvances() {
        val state = game(501).stage(triple(20), triple(19), single(20)).confirm() // 137
        assertEquals(1, state.currentPlayerIndex)
        assertTrue(state.currentDarts.isEmpty())
        assertEquals(501 - 137, state.players[0].remaining)
        assertEquals(137, state.players[0].turns.first().scored)
    }

    @Test
    fun undoRemovesLastDart() {
        val state = X01Engine.undoLastDart(game(501).stage(triple(20), triple(20)))
        assertEquals(1, state.currentDarts.size)
        assertEquals(501 - 60, state.currentRemaining)
    }

    @Test
    fun bustDetectedImmediatelyButAppliedOnConfirm() {
        val staged = game(20).stage(triple(20)) // 60 > 20
        assertEquals(TurnOutcome.BUST, X01Engine.outcomeOf(staged))
        assertEquals(0, staged.currentPlayerIndex) // throw has not passed on yet
        assertEquals(20, staged.players[0].remaining)

        val confirmed = staged.confirm()
        assertEquals(1, confirmed.currentPlayerIndex)
        assertEquals(20, confirmed.players[0].remaining) // score preserved
        assertTrue(confirmed.players[0].turns.first().isBust)
    }

    @Test
    fun bustBlocksFurtherDarts() {
        val staged = game(20).stage(triple(20))
        val blocked = X01Engine.throwDart(staged, single(5))
        assertEquals(staged, blocked)
    }

    @Test
    fun finishingOnNonDoubleIsBustWithDoubleOut() {
        val staged = game(20).stage(single(20)) // exactly zero, but not on a double
        assertEquals(TurnOutcome.BUST, X01Engine.outcomeOf(staged))
    }

    @Test
    fun leavingOneIsBustWithDoubleOut() {
        val staged = game(3).stage(single(2)) // leaves 1
        assertEquals(TurnOutcome.BUST, X01Engine.outcomeOf(staged))
    }

    @Test
    fun checkoutOnDoubleFinishesOnConfirm() {
        val staged = game(40).stage(double(20))
        assertEquals(TurnOutcome.CHECKOUT, X01Engine.outcomeOf(staged))
        assertEquals(GameStatus.IN_PROGRESS, staged.status) // not over until confirmed

        val confirmed = staged.confirm()
        assertEquals(GameStatus.FINISHED, confirmed.status)
        assertEquals("p1", confirmed.winnerId)
        assertEquals(alice, confirmed.winner)
        assertEquals(0, confirmed.players[0].remaining)
    }

    @Test
    fun checkoutWithDoubleBull() {
        val confirmed = game(50).stage(Dart.DoubleBull).confirm()
        assertEquals(GameStatus.FINISHED, confirmed.status)
        assertEquals("p1", confirmed.winnerId)
    }

    @Test
    fun straightOutAllowsFinishingOnSingle() {
        val staged = game(20, doubleOut = false).stage(single(20))
        assertEquals(TurnOutcome.CHECKOUT, X01Engine.outcomeOf(staged))
        assertEquals(GameStatus.FINISHED, staged.confirm().status)
    }

    @Test
    fun checkoutMidTurnBlocksThirdDart() {
        val staged = game(100).stage(triple(20), double(20)) // 60 then 40 → checkout on dart two
        assertEquals(TurnOutcome.CHECKOUT, X01Engine.outcomeOf(staged))
        val blocked = X01Engine.throwDart(staged, single(1))
        assertEquals(staged, blocked)
        assertEquals(2, staged.confirm().players[0].turns.first().darts.size)
    }

    @Test
    fun actionsAfterFinishAreIgnored() {
        val finished = game(40).stage(double(20)).confirm()
        assertEquals(finished, X01Engine.throwDart(finished, triple(20)))
        assertEquals(finished, X01Engine.confirmTurn(finished))
        assertEquals(finished, X01Engine.undoLastDart(finished))
    }

    @Test
    fun confirmingAnOngoingTurnDoesNothing() {
        val staged = game(501).stage(single(5)) // one dart, unsettled
        assertEquals(staged, staged.confirm())
    }

    @Test
    fun turnRotatesBackToFirstPlayer() {
        var state = game(501)
        state = state.stage(single(1), single(1), single(1)).confirm()
        assertEquals(1, state.currentPlayerIndex)
        state = state.stage(single(1), single(1), single(1)).confirm()
        assertEquals(0, state.currentPlayerIndex)
    }

    @Test
    fun threeDartAverageTracksScoredPoints() {
        val state = game(501).stage(triple(20), triple(20), triple(20)).confirm() // 180
        assertEquals(180.0, state.players[0].threeDartAverage)
    }
}
