package com.batu.simpledarttracker.domain.engine

import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.GameConfig
import com.batu.simpledarttracker.domain.model.GameStatus
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.model.Turn
import com.batu.simpledarttracker.domain.model.TurnOutcome
import com.batu.simpledarttracker.domain.model.X01GameState
import com.batu.simpledarttracker.domain.model.X01PlayerState

/**
 * Pure engine for the X01 (501/301/…) rules. Independent of the UI.
 *
 * Flow: the player *stages* up to three darts with [throwDart] (nothing is applied yet)
 * and can take them back with [undoLastDart]; [confirmTurn] applies the turn and passes
 * the throw on. A bust or checkout is recognised by [outcomeOf] the moment the dart lands
 * and further darts are blocked, but the score only changes on confirmation.
 */
object X01Engine {

    /** Starts a new game with the given configuration and players. */
    fun newGame(config: GameConfig, players: List<Player>): X01GameState {
        require(players.isNotEmpty()) { "A game needs at least one player" }
        return X01GameState(
            config = config,
            players = players.map { X01PlayerState(player = it, remaining = config.startingScore) },
        )
    }

    /** State of the turn in progress, based on the staged darts. */
    fun outcomeOf(state: X01GameState): TurnOutcome {
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
            if (remaining == 0) return TurnOutcome.CHECKOUT
        }
        return if (state.currentDarts.size >= 3) TurnOutcome.COMPLETE else TurnOutcome.ONGOING
    }

    /** Stages one dart. Ignored once the turn is settled (three darts, bust or checkout). */
    fun throwDart(state: X01GameState, dart: Dart): X01GameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (outcomeOf(state) != TurnOutcome.ONGOING) return state
        return state.copy(currentDarts = state.currentDarts + dart)
    }

    /** Takes back the last staged (unconfirmed) dart. */
    fun undoLastDart(state: X01GameState): X01GameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (state.currentDarts.isEmpty()) return state
        return state.copy(currentDarts = state.currentDarts.dropLast(1))
    }

    /**
     * Confirms the turn: applies the score and, unless the game is over, passes the throw on.
     * Does nothing while the turn is unsettled ([TurnOutcome.ONGOING]).
     */
    fun confirmTurn(state: X01GameState): X01GameState {
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

            TurnOutcome.CHECKOUT -> commitTurn(
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
