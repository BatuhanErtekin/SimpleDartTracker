package com.batu.simpledarttracker.domain.model

/**
 * State of a single player within an X01 game.
 *
 * [remaining] is the score left after *completed* turns, i.e. the starting value of the
 * next turn. Darts of the turn in progress are kept separately in the game state
 * (see [X01GameState.currentDarts]).
 */
data class X01PlayerState(
    val player: Player,
    val remaining: Int,
    val turns: List<Turn> = emptyList(),
) {
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
 * The complete snapshot of an X01 game. The engine
 * ([com.batu.simpledarttracker.domain.engine.X01Engine]) takes this state and returns a new
 * one — immutable and pure.
 */
data class X01GameState(
    val config: GameConfig,
    val players: List<X01PlayerState>,
    val currentPlayerIndex: Int = 0,
    val currentDarts: List<Dart> = emptyList(),
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val winnerId: String? = null,
) {
    /** The player whose turn it is. */
    val currentPlayer: X01PlayerState get() = players[currentPlayerIndex]

    /** Score left once the staged darts are subtracted — the live value shown on screen. */
    val currentRemaining: Int get() = currentPlayer.remaining - currentDarts.sumOf { it.value }

    /** Points scored so far in the turn in progress. */
    val currentTurnScore: Int get() = currentDarts.sumOf { it.value }

    /** The winning player, if there is one. */
    val winner: Player? get() = winnerId?.let { id -> players.firstOrNull { it.player.id == id }?.player }
}

/**
 * State of the turn in progress (not yet confirmed). The UI drives the undo/confirm
 * buttons and the "BUST/CHECKOUT" badge from it.
 */
enum class TurnOutcome {
    /** Fewer than three darts thrown; the turn can continue. */
    ONGOING,

    /** Three darts thrown, ready to confirm. */
    COMPLETE,

    /** The turn busted; it scores zero once confirmed. */
    BUST,

    /** An exact finish (checkout); confirming it wins the game. */
    CHECKOUT,
}
