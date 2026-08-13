package com.batu.simpledarttracker.domain.x01

import com.batu.simpledarttracker.domain.game.GamePlayerState
import com.batu.simpledarttracker.domain.game.GameState
import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.model.Turn

/**
 * State of a single player within an X01 game.
 *
 * [remaining] is the score left after *completed* turns, i.e. the starting value of the next
 * turn. Darts of the turn in progress are kept separately in the game state
 * (see [X01GameState.currentDarts]).
 */
data class X01PlayerState(
    override val player: Player,
    val remaining: Int,
    val turns: List<Turn> = emptyList(),
) : GamePlayerState {

    /** Total darts this player has thrown, over completed turns. */
    val dartsThrown: Int get() = turns.sumOf { it.darts.size }

    /** Total points credited (a busted turn counts as zero). */
    val pointsScored: Int get() = turns.sumOf { it.scored }

    /** Three-dart average; zero before the first dart. */
    val threeDartAverage: Double
        get() = if (dartsThrown == 0) 0.0 else pointsScored.toDouble() / dartsThrown * 3.0

    /** Whether the player has checked out (nothing remaining). */
    val hasFinished: Boolean get() = remaining == 0
}

/**
 * The complete snapshot of an X01 game. The engine ([X01Engine]) takes this state and returns
 * a new one — immutable and pure.
 */
data class X01GameState(
    val config: X01Config,
    override val players: List<X01PlayerState>,
    override val currentPlayerIndex: Int = 0,
    override val currentDarts: List<Dart> = emptyList(),
    override val status: GameStatus = GameStatus.IN_PROGRESS,
    override val winnerId: String? = null,
) : GameState<X01PlayerState> {

    /** The player whose turn it is. */
    val currentPlayer: X01PlayerState get() = players[currentPlayerIndex]

    /** Score left once the staged darts are subtracted — the live value shown on screen. */
    val currentRemaining: Int get() = currentPlayer.remaining - currentDarts.sumOf { it.value }

    /** Points scored so far in the turn in progress. */
    val currentTurnScore: Int get() = currentDarts.sumOf { it.value }
}
