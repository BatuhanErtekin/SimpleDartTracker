package com.batu.simpledarttracker.domain.game

/**
 * State of the turn in progress, before it is confirmed. The board drives its undo/confirm
 * buttons and its badge from this.
 */
enum class TurnOutcome {
    /** Fewer than three darts thrown; the turn can continue. */
    ONGOING,

    /** Three darts thrown, ready to confirm. */
    COMPLETE,

    /** The turn is void and will score nothing. Only X01 can bust; Cricket never returns this. */
    BUST,

    /** The throw wins the game; confirming it ends the match. */
    WIN,
}

/**
 * The turn protocol every game shares: darts are staged one at a time, and the turn is applied
 * when it is confirmed. Once a turn is settled — three darts thrown, busted, or won — further
 * darts are ignored.
 *
 * Taking a dart back is deliberately absent. Callers hold whole states, so stepping back is
 * just restoring the previous one, which works across a turn boundary too.
 *
 * [I] is what one entry looks like. X01 only needs the dart; Cricket also needs to know which
 * target the player is claiming it for, since one dart can serve several.
 *
 * Starting a game is deliberately not part of this contract, because each game is configured
 * differently. Everything after the first throw is the same.
 */
interface TurnEngine<S : GameState<*>, I> {
    fun outcomeOf(state: S): TurnOutcome

    /** Stages one throw. Ignored once the turn is settled. */
    fun throwDart(state: S, input: I): S

    /** Applies the turn and, unless the game is over, passes the throw on. */
    fun confirmTurn(state: S): S
}
