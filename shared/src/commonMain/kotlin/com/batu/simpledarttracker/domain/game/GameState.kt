package com.batu.simpledarttracker.domain.game

import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player

/** Lifecycle state of a game. */
enum class GameStatus { IN_PROGRESS, FINISHED, ABANDONED }

/**
 * One player's state, as seen by code that does not care which game is being played. Only the
 * identity is shared; what a player accumulates — a remaining total, a grid of marks — is the
 * game's own business.
 */
interface GamePlayerState {
    val player: Player
}

/**
 * The part of a game's state that is the same whatever the rules are: whose throw it is, which
 * darts are staged but not yet confirmed, and whether anybody has won. Each game adds its own
 * scoring on top through [P].
 */
interface GameState<out P : GamePlayerState> {
    val players: List<P>
    val currentPlayerIndex: Int

    /** Darts thrown in the turn in progress; not applied until the turn is confirmed. */
    val currentDarts: List<Dart>
    val status: GameStatus
    val winnerId: String?
}

/** The winning player, if there is one. */
val GameState<*>.winner: Player?
    get() = winnerId?.let { id -> players.firstOrNull { it.player.id == id }?.player }
