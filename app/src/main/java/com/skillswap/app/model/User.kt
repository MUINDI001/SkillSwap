package com.skillswap.app.model

data class User(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val skillsOffered: List<String> = emptyList(),
    val skillsWanted: List<String> = emptyList(),
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val bio: String = "",
    val profileImageUrl: String? = null,
    val title: String = "",
    val email: String = "",
    val phone: String = "",
    val portfolioUrl: String = "",
    val certifications: List<String> = emptyList(),
    val yearsOfExperience: Int = 0
)