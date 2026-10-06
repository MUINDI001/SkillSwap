package com.skillswap.app.model

data class Swap(
    val id: String,
    val fromUser: User,
    val toUser: User,
    val offeredSkill: String,
    val requestedSkill: String,
    var status: String // "pending", "accepted", "completed"
)