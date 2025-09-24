package com.raouf_developer.chatappwithwebssocketcmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform