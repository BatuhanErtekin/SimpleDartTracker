package com.batu.simpledarttracker.domain.model

/**
 * Game modes offered on the home screen. X01 covers 301/501/custom through the
 * same engine with a different starting score; Cricket and Training are separate flows.
 */
enum class GameMode {
    X01,
    CRICKET,
    TRAINING,
}
