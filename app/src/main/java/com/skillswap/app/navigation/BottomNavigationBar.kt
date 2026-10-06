package com.skillswap.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.skillswap.app.repository.FakeRepository

@Composable
fun SoftBottomBar(navController: NavController) {

    val items = listOf(
        NavigationItem(Screen.Home, Icons.Default.Home),
        NavigationItem(Screen.Discover, Icons.Default.Search),
        NavigationItem(Screen.Requests, Icons.Default.Notifications),
        NavigationItem(Screen.Profile, Icons.Default.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = when (item.screen) {
                Screen.Profile -> currentRoute?.startsWith("profile") == true
                else -> currentRoute == item.screen.route
            }
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    val targetRoute = if (item.screen == Screen.Profile) {
                        Screen.Profile.createRoute(FakeRepository.getCurrentUser().id)
                    } else {
                        item.screen.route
                    }
                    
                    if (item.screen == Screen.Home) {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    } else {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.screen.title
                    )
                },
                label = { 
                    Text(
                        text = item.screen.title,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            )
        }
    }
}

private data class NavigationItem(val screen: Screen, val icon: ImageVector)
