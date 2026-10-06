package com.skillswap.app.navigation

sealed class Screen(val route: String, val title: String) {

    object Auth : Screen("auth", "Auth")

    object Home : Screen("home", "Home")

    object Discover : Screen("discover", "Discover")

    object Requests : Screen("requests", "Requests")

    object Profile : Screen("profile/{userId}", "Profile") {
        fun createRoute(userId: String): String {
            return "profile/$userId"
        }
    }

    object EditProfile : Screen("edit_profile/{userId}", "Edit Profile") {
        fun createRoute(userId: String): String {
            return "edit_profile/$userId"
        }
    }
}
