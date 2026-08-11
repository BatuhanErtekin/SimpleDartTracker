package com.batu.simpledarttracker.domain.model

/** A player. [id] is a UUID so records stay unique if we later sync to the cloud. */
data class Player(
    val id: String,
    val name: String,
)
