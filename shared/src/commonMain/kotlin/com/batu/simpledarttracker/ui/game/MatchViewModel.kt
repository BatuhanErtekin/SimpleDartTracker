package com.batu.simpledarttracker.ui.game

import androidx.lifecycle.ViewModel
import com.batu.simpledarttracker.domain.game.GameState
import com.batu.simpledarttracker.domain.game.TurnEngine
import com.batu.simpledarttracker.domain.game.TurnOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How many steps back a match can be walked. Far more than anyone reaches for in practice. */
private const val HISTORY_LIMIT = 64

/**
 * Owns the match in progress for one game, and nothing else: the rules stay in the engine, and
 * this only holds the current state and forwards intents to it. Keeping the state here is what
 * lets a game survive a rotation.
 *
 * It talks to the [TurnEngine] abstraction rather than to a particular engine, so every game
 * shares this plumbing and adds only what is genuinely its own — how a match starts, and any
 * moves outside the turn protocol.
 *
 * There is no confirm step. A turn lands the moment it resolves, so the board keeps up with
 * the darts as they are entered. Mistakes are handled by [undo], which walks back through
 * whole states and so can step over a turn boundary — something dropping a staged dart could
 * never do.
 *
 * [game] is null when no match is running.
 */
abstract class MatchViewModel<S : GameState<*>, I>(
    private val engine: TurnEngine<S, I>,
) : ViewModel() {

    private val _game = MutableStateFlow<S?>(null)
    val game: StateFlow<S?> = _game.asStateFlow()

    /**
     * The turn that just landed, kept until the next dart is thrown.
     *
     * Without a confirm step the board would otherwise clear the moment a turn resolves, and a
     * bust would flash past unread. Holding the resolved state lets the panel go on showing
     * what was thrown, and why the throw passed on.
     */
    private val _lastTurn = MutableStateFlow<S?>(null)
    val lastTurn: StateFlow<S?> = _lastTurn.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val history = ArrayDeque<S>()

    fun throwDart(input: I) = record { state ->
        val staged = engine.throwDart(state, input)
        if (staged == state) state else settle(staged)
    }

    /**
     * Lets an entry land. While the turn still has darts left it simply stands; once it is
     * resolved it is remembered for the panel and applied, because there is no confirm step.
     */
    protected fun settle(staged: S): S =
        if (engine.outcomeOf(staged) == TurnOutcome.ONGOING) {
            _lastTurn.value = null
            staged
        } else {
            _lastTurn.value = staged
            engine.confirmTurn(staged)
        }

    /** Steps back one entry, across a turn boundary if that is where the mistake was. */
    fun undo() {
        val previous = history.removeLastOrNull() ?: return
        _game.value = previous
        _lastTurn.value = null
        _canUndo.value = history.isNotEmpty()
    }

    fun end() {
        _game.value = null
        reset()
    }

    /** Replaces the match outright — starting one, or starting the same one again. */
    protected fun setGame(state: S) {
        _game.value = state
        reset()
    }

    /**
     * Applies [transform] to the running match and remembers where it came from. A transform
     * that changes nothing — a dart the engine refused — is not worth an undo step.
     */
    protected fun record(transform: (S) -> S) {
        val current = _game.value ?: return
        val next = transform(current)
        if (next == current) return

        history.addLast(current)
        if (history.size > HISTORY_LIMIT) history.removeFirst()
        _canUndo.value = true
        _game.value = next
    }

    private fun reset() {
        history.clear()
        _canUndo.value = false
        _lastTurn.value = null
    }
}
