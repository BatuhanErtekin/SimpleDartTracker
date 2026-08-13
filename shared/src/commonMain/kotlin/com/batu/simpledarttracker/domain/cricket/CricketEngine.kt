package com.batu.simpledarttracker.domain.cricket

import com.batu.simpledarttracker.domain.game.GameStatus
import com.batu.simpledarttracker.domain.game.TurnEngine
import com.batu.simpledarttracker.domain.game.TurnOutcome
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.model.isHouse

/**
 * Pure engine for Cricket. Independent of the UI, and following the shared [TurnEngine]
 * protocol: throws are staged, can be taken back, and only land on confirmation.
 *
 * A target closes once a player has its required marks. Marks beyond that spill over into
 * points — but only while at least one opponent still has the target open; once everybody has
 * closed it the target is dead and pays nothing. Where those points land depends on
 * [CricketScoring]: they either reward the thrower or, in cut-throat, punish everyone who has
 * not closed the target yet.
 *
 * Cricket cannot bust. A turn ends by running out of darts or by winning outright, which is
 * spotted the moment it happens so the rest of the turn is blocked, exactly as a checkout is
 * in X01.
 */
object CricketEngine : TurnEngine<CricketGameState, CricketThrow> {

    /** Starts a new game with the given configuration and players. */
    fun newGame(config: CricketConfig, players: List<Player>): CricketGameState {
        require(players.isNotEmpty()) { "A game needs at least one player" }
        return CricketGameState(
            config = config,
            players = players.map { CricketPlayerState(player = it) },
        )
    }

    /**
     * Moves a player to a different seat, keeping the throw with whoever holds it. Mirrors the
     * X01 behaviour so the in-match settings work the same in both games.
     */
    fun movePlayer(state: CricketGameState, fromIndex: Int, toIndex: Int): CricketGameState {
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

    /**
     * Whether one more mark on [target] would spill into points for [playerIndex] — they have
     * it closed, and somebody else has not. Anything already staged counts, so the answer keeps
     * up within a turn.
     *
     * The board uses this to decide when it has to ask what was actually thrown: while a mark
     * is only a mark, the detail changes nothing, so there is nothing worth asking.
     */
    fun wouldScore(
        state: CricketGameState,
        target: CricketTarget,
        playerIndex: Int = state.currentPlayerIndex,
    ): Boolean {
        if (target !in state.config.targets) return false
        if (playerIndex !in state.players.indices) return false
        val players = replayTurn(state).players
        if (!isClosed(players[playerIndex], target, state.config)) return false
        return players.indices.any {
            it != playerIndex && !isClosed(players[it], target, state.config)
        }
    }

    /**
     * Hands the throw to [playerIndex], applying whatever the current player had already
     * entered.
     *
     * A visit does not have to be three darts on the board: the moment somebody else steps up,
     * the previous one is over, however much of it was recorded. So the board passes the turn
     * when a tap lands in another player's column, and this closes the old one honestly rather
     * than discarding it.
     */
    fun passTurnTo(state: CricketGameState, playerIndex: Int): CricketGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (playerIndex !in state.players.indices) return state
        if (playerIndex == state.currentPlayerIndex) return state

        val settled = replayTurn(state)
        // A won turn ends the match; nobody is taking the throw off them.
        if (settled.won) return confirmTurn(state)

        return state.copy(
            players = settled.players,
            currentThrows = emptyList(),
            houseClaimed = false,
            currentPlayerIndex = playerIndex,
        )
    }

    /**
     * The match as it would stand if the staged turn landed now — marks and scores included.
     * For display only: the board draws from this so a mark appears the moment it is entered.
     */
    fun preview(state: CricketGameState): CricketGameState =
        state.copy(players = replayTurn(state).players)

    override fun outcomeOf(state: CricketGameState): TurnOutcome {
        val settled = replayTurn(state)
        if (settled.won) return TurnOutcome.WIN
        return if (state.currentThrows.size >= 3) TurnOutcome.COMPLETE else TurnOutcome.ONGOING
    }

    override fun throwDart(state: CricketGameState, input: CricketThrow): CricketGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (outcomeOf(state) != TurnOutcome.ONGOING) return state
        val target = input.target
        if (target != null) {
            // A claim has to be one this match can honour; a miss carries no claim at all.
            if (target !in state.config.targets) return state
            if (marksFor(input.dart, target) == 0) return state
        }
        return state.copy(currentThrows = state.currentThrows + input)
    }

    /** Whether this match plays House at all. */
    fun playsHouse(state: CricketGameState): Boolean =
        CricketTarget.House in state.config.targets

    /**
     * Records a house: the whole turn in one go, from the three darts the player reports.
     *
     * A house is only known once all three darts are thrown, and its worth is their combined
     * value, so the board collects them together rather than one at a time. Whatever was
     * already staged for this turn is replaced.
     *
     * Claiming spends the whole turn on the house: the darts do not also mark the bed they
     * landed on. They are counted once, as the house, which is why the house is worth their
     * total — marking the bed as well would pay for the same three darts twice.
     */
    fun claimHouse(state: CricketGameState, darts: List<Dart>): CricketGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (!playsHouse(state)) return state
        if (!isHouse(darts)) return state

        return state.copy(
            currentThrows = darts.map { CricketThrow(it, null) },
            houseClaimed = true,
        )
    }

    override fun confirmTurn(state: CricketGameState): CricketGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (outcomeOf(state) == TurnOutcome.ONGOING) return state

        val settled = replayTurn(state)
        val idx = state.currentPlayerIndex

        if (settled.won) {
            return state.copy(
                players = settled.players,
                currentThrows = emptyList(),
                houseClaimed = false,
                status = GameStatus.FINISHED,
                winnerId = state.currentPlayer.player.id,
            )
        }
        return state.copy(
            players = settled.players,
            currentThrows = emptyList(),
            houseClaimed = false,
            currentPlayerIndex = (idx + 1) % state.players.size,
        )
    }

    /** The staged turn applied to the players, plus whether it won the game. */
    private data class Settled(val players: List<CricketPlayerState>, val won: Boolean)

    /**
     * Replays the staged turn onto a copy of the players. Throws are applied in order because
     * each one can change what the next is worth — closing a target turns the rest of the turn
     * into points — and the win is checked after every step so a mid-turn finish is caught.
     */
    private fun replayTurn(state: CricketGameState): Settled {
        val config = state.config
        val idx = state.currentPlayerIndex
        var players = state.players

        for (thrown in state.currentThrows) {
            players = applyThrow(players, idx, thrown, config)
            if (hasWon(players, idx, config)) return Settled(players, won = true)
        }
        if (state.houseClaimed) {
            players = applyHouse(players, idx, state.currentDarts, config)
            if (hasWon(players, idx, config)) return Settled(players, won = true)
        }
        return Settled(players, won = false)
    }

    private fun applyThrow(
        players: List<CricketPlayerState>,
        idx: Int,
        thrown: CricketThrow,
        config: CricketConfig,
    ): List<CricketPlayerState> {
        val target = thrown.target ?: return players
        if (target !in config.targets) return players
        val gained = marksFor(thrown.dart, target)
        if (gained == 0) return players
        return applyMarks(
            players = players,
            idx = idx,
            target = target,
            gained = gained,
            pointsPerMark = pointsPerMark(thrown.dart, target),
            config = config,
        )
    }

    /**
     * A claimed house is worth one mark, and the whole turn's darts in points — 20, D20 and T20
     * on the same bed pay 120.
     */
    private fun applyHouse(
        players: List<CricketPlayerState>,
        idx: Int,
        darts: List<Dart>,
        config: CricketConfig,
    ): List<CricketPlayerState> = applyMarks(
        players = players,
        idx = idx,
        target = CricketTarget.House,
        gained = 1,
        pointsPerMark = darts.sumOf { it.value },
        config = config,
    )

    /**
     * Puts [gained] marks on [target] for player [idx]. Whatever the target could not absorb
     * spills over as points, at [pointsPerMark] each.
     */
    private fun applyMarks(
        players: List<CricketPlayerState>,
        idx: Int,
        target: CricketTarget,
        gained: Int,
        pointsPerMark: Int,
        config: CricketConfig,
    ): List<CricketPlayerState> {
        val thrower = players[idx]
        val required = config.requiredMarks(target)
        val before = thrower.marksOn(target)
        val used = minOf(gained, (required - before).coerceAtLeast(0))
        val excess = gained - used

        val updated = players.toMutableList()
        if (used > 0) {
            updated[idx] = thrower.copy(marks = thrower.marks + (target to before + used))
        }
        if (excess == 0) return updated

        // A target everybody has closed is dead: no points change hands.
        val stillOpen = updated.indices.filter { it != idx && !isClosed(updated[it], target, config) }
        if (stillOpen.isEmpty()) return updated

        val points = excess * pointsPerMark
        when (config.scoring) {
            CricketScoring.STANDARD ->
                updated[idx] = updated[idx].copy(score = updated[idx].score + points)

            CricketScoring.CUT_THROAT ->
                stillOpen.forEach { updated[it] = updated[it].copy(score = updated[it].score + points) }
        }
        return updated
    }

    private fun isClosed(
        player: CricketPlayerState,
        target: CricketTarget,
        config: CricketConfig,
    ): Boolean = player.marksOn(target) >= config.requiredMarks(target)

    /**
     * A player wins by closing everything and being level or ahead on points — level or behind
     * when points are a punishment.
     */
    private fun hasWon(
        players: List<CricketPlayerState>,
        idx: Int,
        config: CricketConfig,
    ): Boolean {
        val me = players[idx]
        if (!config.targets.all { isClosed(me, it, config) }) return false
        return when (config.scoring) {
            CricketScoring.STANDARD -> players.all { it.score <= me.score }
            CricketScoring.CUT_THROAT -> players.all { it.score >= me.score }
        }
    }
}
