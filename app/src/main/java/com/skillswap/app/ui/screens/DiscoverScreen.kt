@file:OptIn(ExperimentalMaterial3Api::class)

package com.skillswap.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.skillswap.app.navigation.Screen
import com.skillswap.app.ui.components.LoadingState
import com.skillswap.app.ui.components.UserListItem
import com.skillswap.app.viewmodel.DiscoverUiState
import com.skillswap.app.viewmodel.DiscoverViewModel
import com.skillswap.app.viewmodel.UserSearchResult

@Composable
fun DiscoverScreen(
    navController: NavHostController,
    viewModel: DiscoverViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNeighborhood by viewModel.selectedNeighborhood.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val neighborhoods = listOf("All", "East Legon", "Osu", "Madina", "Spintex", "Adenta", "Tema")
    val categories = listOf("Coding", "UI/UX Design", "Photography", "Graphic Design", "Business", "Cooking")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Discover Local Experts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Across Accra, Ghana 🇬🇭",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar Section
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )

            // Neighborhood Filter Chips
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    "NEIGHBORHOOD IN ACCRA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(neighborhoods) { area ->
                        FilterChip(
                            selected = selectedNeighborhood == area,
                            onClick = { viewModel.onNeighborhoodSelected(area) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (area != "All") {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(area)
                                }
                            },
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }

            // Category Chips
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    "SKILL CATEGORIES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = searchQuery.equals(category, ignoreCase = true),
                            onClick = {
                                if (searchQuery.equals(category, ignoreCase = true)) viewModel.onSearchQueryChanged("")
                                else viewModel.onSearchQueryChanged(category)
                            },
                            label = { Text(category) },
                            shape = MaterialTheme.shapes.medium,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "DiscoverStateTransition"
                ) { state ->
                    when (state) {
                        is DiscoverUiState.Idle, is DiscoverUiState.Loading -> {
                            LoadingState(message = "Finding skills in Accra...")
                        }
                        is DiscoverUiState.Empty -> {
                            DiscoverEmptyState(searchQuery, selectedNeighborhood)
                        }
                        is DiscoverUiState.Success -> {
                            DiscoverUserList(
                                searchResults = state.searchResults,
                                onUserClick = { user ->
                                    navController.navigate(Screen.Profile.createRoute(user.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search by skill, name, or area...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
fun DiscoverUserList(searchResults: List<UserSearchResult>, onUserClick: (com.skillswap.app.model.User) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(searchResults, key = { it.user.id }) { result ->
            UserListItem(
                user = result.user,
                onSwapClick = { onUserClick(result.user) },
                onCardClick = { onUserClick(result.user) },
                matchTag = result.matchReason.ifBlank { null }
            )
        }
    }
}

@Composable
fun DiscoverEmptyState(query: String, neighborhood: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🇬🇭", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isNotEmpty()) "No results for \"$query\"" else "No experts found in $neighborhood",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We couldn't find any matching skill exchanges in Accra. Try searching for another skill or clearing filters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
