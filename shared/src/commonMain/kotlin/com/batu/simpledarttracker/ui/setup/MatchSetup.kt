package com.batu.simpledarttracker.ui.setup

import com.batu.simpledarttracker.domain.cricket.CricketConfig
import com.batu.simpledarttracker.domain.model.Player
import com.batu.simpledarttracker.domain.x01.X01Config

/**
 * Everything the setup screen collected, ready to start a match. Each game contributes its own
 * variant; the player list is the part they all share.
 */
sealed interface MatchSetup {
    val players: List<Player>

    data class X01(
        override val players: List<Player>,
        val config: X01Config,
    ) : MatchSetup

    data class Cricket(
        override val players: List<Player>,
        val config: CricketConfig,
    ) : MatchSetup
}
