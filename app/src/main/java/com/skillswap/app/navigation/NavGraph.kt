package com.skillswap.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.skillswap.app.repository.FakeRepository
import com.skillswap.app.ui.screens.*
import com.skillswap.app.viewmodel.AuthState
import com.skillswap.app.viewmodel.AuthViewModel
import com.skillswap.app.viewmodel.DiscoverViewModel
import com.skillswap.app.viewmodel.HomeViewModel
import com.skillswap.app.viewmodel.RequestsViewModel

@Composable
fun MainScreen() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Authenticated -> {
            AuthenticatedMainContent(authViewModel = authViewModel)
        }
        else -> {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = { /* State observation handles transition automatically */ }
            )
        }
    }
}

@Composable
fun AuthenticatedMainContent(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val requestsViewModel: RequestsViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val discoverViewModel: DiscoverViewModel = viewModel()

    Scaffold(
        bottomBar = {
            SoftBottomBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = { 
                fadeIn(animationSpec = tween(150)) + slideInHorizontally(animationSpec = tween(150)) { it } 
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(150)) + slideOutHorizontally(animationSpec = tween(150)) { -it } 
            },
            popEnterTransition = { 
                fadeIn(animationSpec = tween(150)) + slideInHorizontally(animationSpec = tween(150)) { -it } 
            },
            popExitTransition = { 
                fadeOut(animationSpec = tween(150)) + slideOutHorizontally(animationSpec = tween(150)) { it }
            }
        ) {

            // ---------------- HOME ----------------
            composable(Screen.Home.route) {
                HomeScreen(
                    onUserClick = { user ->
                        navController.navigate(Screen.Profile.createRoute(user.id))
                    },
                    onCategoryClick = { category ->
                        discoverViewModel.onSearchQueryChanged(category)
                        navController.navigate(Screen.Discover.route)
                    },
                    viewModel = homeViewModel
                )
            }

            // ---------------- DISCOVER ----------------
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    navController = navController,
                    viewModel = discoverViewModel
                )
            }

            // ---------------- REQUESTS ----------------
            composable(Screen.Requests.route) {
                RequestsScreen(
                    navController = navController,
                    viewModel = requestsViewModel
                )
            }

            // ---------------- PROFILE ----------------
            composable(
                route = Screen.Profile.route,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                
                ProfileScreen(
                    navController = navController,
                    userId = userId,
                    onSwapRequest = { targetUser ->
                        val currentUser = FakeRepository.getCurrentUser()
                        if (targetUser.id != currentUser.id) {
                            requestsViewModel.sendRequest(currentUser, targetUser)
                        }
                    },
                    onEditProfileClick = { editUserId ->
                        navController.navigate(Screen.EditProfile.createRoute(editUserId))
                    },
                    onSignOutClick = {
                        authViewModel.signOut()
                    }
                )
            }

            // ---------------- EDIT PROFILE ----------------
            composable(
                route = Screen.EditProfile.route,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                
                EditProfileScreen(
                    navController = navController,
                    userId = userId
                )
            }
        }
    }
}
