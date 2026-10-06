@file:OptIn(ExperimentalMaterial3Api::class)

package com.skillswap.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.skillswap.app.model.Swap
import com.skillswap.app.model.User
import com.skillswap.app.navigation.Screen
import com.skillswap.app.repository.FakeRepository
import com.skillswap.app.ui.components.ActiveSwapDialog
import com.skillswap.app.ui.components.LoadingState
import com.skillswap.app.viewmodel.RequestsUiState
import com.skillswap.app.viewmodel.RequestsViewModel

@Composable
fun RequestsScreen(
    navController: NavHostController,
    viewModel: RequestsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = FakeRepository.getCurrentUser()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Incoming, 1: Sent, 2: Active
    var activeSwapToChat by remember { mutableStateOf<Swap?>(null) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Skill Swaps",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Incoming", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Sent", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Active Swaps", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "RequestsStateTransition"
            ) { state ->
                when (state) {
                    is RequestsUiState.Loading -> {
                        LoadingState(message = "Loading your swap requests...")
                    }
                    is RequestsUiState.Empty -> {
                        RequestsEmptyState(selectedTab)
                    }
                    is RequestsUiState.Success -> {
                        val allSwaps = state.swaps
                        
                        val filteredSwaps = when (selectedTab) {
                            0 -> allSwaps.filter { it.toUser.id == currentUser.id && it.status == "pending" }
                            1 -> allSwaps.filter { it.fromUser.id == currentUser.id && it.status == "pending" }
                            2 -> allSwaps.filter { (it.fromUser.id == currentUser.id || it.toUser.id == currentUser.id) && it.status == "accepted" }
                            else -> emptyList()
                        }

                        if (filteredSwaps.isEmpty()) {
                            RequestsEmptyState(selectedTab)
                        } else {
                            RequestsList(
                                swaps = filteredSwaps,
                                currentUser = currentUser,
                                selectedTab = selectedTab,
                                onAccept = { viewModel.acceptRequest(it) },
                                onDecline = { viewModel.declineRequest(it) },
                                onUserClick = { user ->
                                    navController.navigate(Screen.Profile.createRoute(user.id))
                                },
                                onOpenChat = { swap ->
                                    activeSwapToChat = swap
                                }
                            )
                        }
                    }
                }
            }

            // Active Swap Communication Modal
            activeSwapToChat?.let { swap ->
                ActiveSwapDialog(
                    swap = swap,
                    currentUser = currentUser,
                    onDismiss = { activeSwapToChat = null }
                )
            }
        }
    }
}

@Composable
fun RequestsEmptyState(tabIndex: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when (tabIndex) {
                            0 -> "📥"
                            1 -> "📤"
                            else -> "🤝"
                        },
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = when (tabIndex) {
                    0 -> "No incoming requests"
                    1 -> "No pending sent requests"
                    else -> "No active swaps yet"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (tabIndex) {
                    0 -> "When members in Accra request a skill swap with you, they'll appear here."
                    1 -> "Requests you send to other members in Accra will be tracked here."
                    else -> "Accepted skill swaps in Accra will appear here for you to coordinate."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RequestsList(
    swaps: List<Swap>,
    currentUser: User,
    selectedTab: Int,
    onAccept: (Swap) -> Unit,
    onDecline: (Swap) -> Unit,
    onUserClick: (User) -> Unit,
    onOpenChat: (Swap) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(swaps, key = { it.id }) { swap ->
            val displayUser = if (swap.fromUser.id == currentUser.id) swap.toUser else swap.fromUser

            SwapRequestCard(
                swap = swap,
                displayUser = displayUser,
                currentUser = currentUser,
                selectedTab = selectedTab,
                onAccept = { onAccept(swap) },
                onDecline = { onDecline(swap) },
                onUserClick = { onUserClick(displayUser) },
                onOpenChat = { onOpenChat(swap) }
            )
        }
    }
}

@Composable
fun SwapRequestCard(
    swap: Swap,
    displayUser: User,
    currentUser: User,
    selectedTab: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onUserClick: () -> Unit,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayUser.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayUser.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = displayUser.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = when (swap.status) {
                        "accepted" -> MaterialTheme.colorScheme.primaryContainer
                        "declined" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = swap.status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (swap.status) {
                            "accepted" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "declined" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Offers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        Text(swap.offeredSkill, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Wants", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        Text(swap.requestedSkill, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    }
                }
            }

            // Actions based on tab
            if (selectedTab == 0 && swap.status == "pending") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline")
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept")
                    }
                }
            } else if (selectedTab == 2 || swap.status == "accepted") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenChat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Active Swap Chat")
                }
            }
        }
    }
}
