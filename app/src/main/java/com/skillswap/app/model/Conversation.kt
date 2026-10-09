package com.skillswap.app.model

data class Conversation(
    val conversationId: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val partnerUser: User? = null
)
