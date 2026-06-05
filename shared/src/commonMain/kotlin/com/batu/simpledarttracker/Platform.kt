package com.batu.simpledarttracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform