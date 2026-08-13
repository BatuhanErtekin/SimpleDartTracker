package com.batu.simpledarttracker.domain.cricket

import com.batu.simpledarttracker.domain.game.GamePlayerState
import com.batu.simpledarttracker.domain.game.GameState
import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player

/** Rules for a game of Cricket. */
data class CricketConfig(
    /** Targets in play, in the order the board shows them. */
    val targets: List<CricketTarget>,
    /** Per-target mark requirements; anything absent takes [DEFAULT_MARKS_TO_CLOSE]. */
    val markOverrides: Map<CricketTarget, Int> = emptyMap(),
    val scoring: CricketScoring = CricketScoring.STANDARD,
) {
    init { require(targets.isNotEmpty()) { "A Cricket game needs at least one target" } }

    /** Marks that close [target] in this match. */
    fun requiredMarks(target: CricketTarget): Int = marksToClose(target, markOverrides)
}

/** State of a single player within a Cricket game. */
data class CricketPlayerState(
    override val player: Player,
    /** Marks accumulated per target; never more than the target requires. */
    val marks: Map<CricketTarget, Int> = emptyMap(),
    val score: Int = 0,
) : GamePlayerState {

    fun marksOn(target: CricketTarget): Int = marks[target] ?: 0
}

/**
 * The complete snapshot of a Cricket game. Like X01 it is immutable and the engine
 * ([CricketEngine]) turns one state into the next.
 *
 * [currentThrows] holds the turn in progress. Each entry carries the target its dart was
 * claimed for, because one dart can serve several and the choice belongs to the player.
 * [houseClaimed] is set only when the player asks for it — a house is never awarded on its own.
 */
data class CricketGameState(
    val config: CricketConfig,
    override val players: List<CricketPlayerState>,
    override val currentPlayerIndex: Int = 0,
    val currentThrows: List<CricketThrow> = emptyList(),
    val houseClaimed: Boolean = false,
    override val status: GameStatus = GameStatus.IN_PROGRESS,
    override val winnerId: String? = null,
) : GameState<CricketPlayerState> {

    override val currentDarts: List<Dart> get() = currentThrows.map { it.dart }

    /** The player whose turn it is. */
    val currentPlayer: CricketPlayerState get() = players[currentPlayerIndex]

    /** Whether [player] has finished [target] off. */
    fun hasClosed(player: CricketPlayerState, target: CricketTarget): Boolean =
        player.marksOn(target) >= config.requiredMarks(target)

    /** A target nobody can score on any more, because everybody has closed it. */
    fun isDead(target: CricketTarget): Boolean = players.all { hasClosed(it, target) }
}
