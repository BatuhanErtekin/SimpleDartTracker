package com.batu.simpledarttracker.domain.x01

import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.game.TurnEngine
import com.batu.simpledarttracker.domain.game.TurnOutcome
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.model.Turn

/**
 * Pure engine for the X01 (501/301/…) rules. Independent of the UI.
 *
 * It follows the shared [TurnEngine] protocol: darts are staged, taken back, then confirmed.
 * A bust or checkout is recognised by [outcomeOf] the moment the dart lands and further darts
 * are blocked, but the score only changes on confirmation.
 */
object X01Engine : TurnEngine<X01GameState, Dart> {

    /** Starts a new game with the given configuration and players. */
    fun newGame(config: X01Config, players: List<Player>): X01GameState {
        require(players.isNotEmpty()) { "A game needs at least one player" }
        return X01GameState(
            config = config,
            players = players.map { X01PlayerState(player = it, remaining = config.startingScore) },
        )
    }

    /**
     * Moves a player to a different seat. The throw stays with whoever currently holds it, so
     * the order can be corrected mid-match without skipping anyone. Out-of-range indices and
     * no-op moves leave the state untouched.
     */
    fun movePlayer(state: X01GameState, fromIndex: Int, toIndex: Int): X01GameState {
        if (fromIndex !in state.players.indices) return state
        if (toIndex !in state.players.indices) return state
        if (fromIndex == toIndex) return state

        val throwerId = state.currentPlayer.player.id
        val reordered = state.players.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        return state.copy(
            players = reordered,
            currentPlayerIndex = reordered.indexOfFirst { it.player.id == throwerId },
        )
    }

    override fun outcomeOf(state: X01GameState): TurnOutcome {
        val doubleOut = state.config.doubleOut
        var remaining = state.currentPlayer.remaining
        for (dart in state.currentDarts) {
            remaining -= dart.value
            val bust = if (doubleOut) {
                remaining < 0 || remaining == 1 || (remaining == 0 && !dart.isDouble)
            } else {
                remaining < 0
            }
            if (bust) return TurnOutcome.BUST
            if (remaining == 0) return TurnOutcome.WIN
        }
        return if (state.currentDarts.size >= 3) TurnOutcome.COMPLETE else TurnOutcome.ONGOING
    }

    override fun throwDart(state: X01GameState, input: Dart): X01GameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (outcomeOf(state) != TurnOutcome.ONGOING) return state
        return state.copy(currentDarts = state.currentDarts + input)
    }

    override fun confirmTurn(state: X01GameState): X01GameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        val outcome = outcomeOf(state)
        val player = state.currentPlayer
        val darts = state.currentDarts
        val turnStart = player.remaining
        val live = turnStart - darts.sumOf { it.value }

        return when (outcome) {
            TurnOutcome.ONGOING -> state

            TurnOutcome.BUST -> commitTurn(
                state = state,
                turn = Turn(player.player.id, darts, isBust = true),
                newRemaining = turnStart, // bust: the score reverts to where the turn started
                advance = true,
            )

            TurnOutcome.WIN -> commitTurn(
                state = state,
                turn = Turn(player.player.id, darts, isBust = false),
                newRemaining = 0,
                advance = false,
                finishedBy = player.player.id,
            )

            TurnOutcome.COMPLETE -> commitTurn(
                state = state,
                turn = Turn(player.player.id, darts, isBust = false),
                newRemaining = live,
                advance = true,
            )
        }
    }

    private fun commitTurn(
        state: X01GameState,
        turn: Turn,
        newRemaining: Int,
        advance: Boolean,
        finishedBy: String? = null,
    ): X01GameState {
        val idx = state.currentPlayerIndex
        val updatedPlayers = state.players.mapIndexed { i, p ->
            if (i == idx) p.copy(remaining = newRemaining, turns = p.turns + turn) else p
        }

        if (finishedBy != null) {
            return state.copy(
                players = updatedPlayers,
                currentDarts = emptyList(),
                status = GameStatus.FINISHED,
                winnerId = finishedBy,
            )
        }

        return state.copy(
            players = updatedPlayers,
            currentDarts = emptyList(),
            currentPlayerIndex = if (advance) (idx + 1) % state.players.size else idx,
        )
    }
}
