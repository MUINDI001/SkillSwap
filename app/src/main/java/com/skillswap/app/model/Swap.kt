package com.skillswap.app.model

data class Swap(
    val id: String = "",
    val fromUser: User = User(),
    val toUser: User = User(),
    val offeredSkill: String = "",
    val requestedSkill: String = "",
    var status: String = "pending",
    val participantIds: List<String> = emptyList()
)
