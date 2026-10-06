package com.skillswap.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Discover : BottomNavItem("discover", Icons.Default.Search, "Discover")
    object Requests : BottomNavItem("requests", Icons.Default.Refresh, "Requests")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}