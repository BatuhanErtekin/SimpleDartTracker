package com.batu.simpledarttracker.ui.cricket

import com.batu.simpledarttracker.domain.cricket.CricketConfig
import com.batu.simpledarttracker.domain.cricket.CricketEngine
import com.batu.simpledarttracker.domain.cricket.CricketGameState
import com.batu.simpledarttracker.domain.cricket.CricketThrow
import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.ui.game.MatchViewModel

/** Owns the Cricket match in progress. See [MatchViewModel] for the shared plumbing. */
class CricketViewModel : MatchViewModel<CricketGameState, CricketThrow>(CricketEngine) {

    fun start(config: CricketConfig, players: List<Player>) {
        setGame(CricketEngine.newGame(config, players))
    }

    /**
     * Records a throw for [playerIndex]. If that is not whoever currently holds the throw, the
     * turn passes to them first — a tap in someone else's column is the board being told that
     * the visit has moved on.
     */
    fun throwFor(playerIndex: Int, thrown: CricketThrow) = record { state ->
        val passed = CricketEngine.passTurnTo(state, playerIndex)
        val staged = CricketEngine.throwDart(passed, thrown)
        if (staged == passed) passed else settle(staged)
    }

    /**
     * Records a house for [playerIndex], passing the turn first if need be. It fills the whole
     * turn, so it resolves it too and lands straight away.
     */
    fun claimHouseFor(playerIndex: Int, darts: List<Dart>) = record { state ->
        val passed = CricketEngine.passTurnTo(state, playerIndex)
        val claimed = CricketEngine.claimHouse(passed, darts)
        if (claimed == passed) passed else settle(claimed)
    }

    /** Reorders the seats without changing whose throw it is. */
    fun movePlayer(fromIndex: Int, toIndex: Int) = record {
        CricketEngine.movePlayer(it, fromIndex, toIndex)
    }

    /** Restarts with the same rules and players. The old match is not something to undo into. */
    fun playAgain() {
        val finished = game.value ?: return
        setGame(CricketEngine.newGame(finished.config, finished.players.map { it.player }))
    }
}
