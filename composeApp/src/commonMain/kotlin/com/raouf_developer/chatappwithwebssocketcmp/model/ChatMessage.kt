package com.raouf_developer.chatappwithwebssocketcmp.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val type: String = "CHAT", // "CHAT", "JOIN", "LEAVE"
    val content: String,
    val sender: String,
    val receiver: String = "",
    val chatRoomId: String = "general",
    val timestamp: Long ="1758500687900".toLong()
)