package com.batu.simpledarttracker.ui.x01

import com.batu.simpledarttracker.domain.model.Dart
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.x01.X01Config
import com.batu.simpledarttracker.domain.x01.X01Engine
import com.batu.simpledarttracker.domain.x01.X01GameState
import com.batu.simpledarttracker.ui.game.MatchViewModel

/** Owns the X01 match in progress. See [MatchViewModel] for the shared plumbing. */
class X01ViewModel : MatchViewModel<X01GameState, Dart>(X01Engine) {

    fun start(config: X01Config, players: List<Player>) {
        setGame(X01Engine.newGame(config, players))
    }

    /** Reorders the seats without changing whose throw it is. */
    fun movePlayer(fromIndex: Int, toIndex: Int) = record {
        X01Engine.movePlayer(it, fromIndex, toIndex)
    }

    /** Restarts with the same rules and players. The old match is not something to undo into. */
    fun playAgain() {
        val finished = game.value ?: return
        setGame(X01Engine.newGame(finished.config, finished.players.map { it.player }))
    }
}
