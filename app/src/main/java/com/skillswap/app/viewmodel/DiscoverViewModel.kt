package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import com.skillswap.app.repository.FirestoreRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserSearchResult(
    val user: User,
    val matchReason: String = "",
    val matchRank: Int = 3
)

sealed class DiscoverUiState {
    object Idle : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Success(val searchResults: List<UserSearchResult>) : DiscoverUiState()
    object Empty : DiscoverUiState()
}

class DiscoverViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNeighborhood = MutableStateFlow("All")
    val selectedNeighborhood: StateFlow<String> = _selectedNeighborhood.asStateFlow()

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Idle)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        observeSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            combine(_searchQuery.debounce(150), _selectedNeighborhood) { query, neighborhood ->
                Pair(query, neighborhood)
            }
            .onEach { (query, neighborhood) ->
                _uiState.value = DiscoverUiState.Loading

                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: FakeRepository.getCurrentUser().id

                val firestoreUsers = try {
                    repository.getAllUsers()
                } catch (e: Exception) {
                    emptyList()
                }

                val localUsers = FakeRepository.getUsers()
                val mergedUsers = (firestoreUsers + localUsers).distinctBy { it.id }

                // Prioritize real signed-up users at the top of Discover
                val prioritizedUsers = mergedUsers.sortedByDescending { if (it.id.length > 10) 2 else 1 }

                val cleanQuery = query.trim().lowercase()

                val results = prioritizedUsers.mapNotNull { user ->
                    if (user.id == currentUid || user.id.isBlank()) return@mapNotNull null

                    val matchesNeighborhood = neighborhood == "All" ||
                            user.location.contains(neighborhood, ignoreCase = true)

                    if (!matchesNeighborhood) return@mapNotNull null

                    if (cleanQuery.isEmpty()) {
                        val isRealUser = user.id.length > 10
                        return@mapNotNull UserSearchResult(
                            user = user,
                            matchReason = if (isRealUser) "New Member" else "",
                            matchRank = if (isRealUser) 1 else 3
                        )
                    }

                    // Check Skills Offered
                    val matchedOffered = user.skillsOffered.firstOrNull { it.lowercase().contains(cleanQuery) }
                    if (matchedOffered != null) {
                        return@mapNotNull UserSearchResult(
                            user = user,
                            matchReason = "Offers $matchedOffered",
                            matchRank = 1
                        )
                    }

                    // Check Skills Wanted
                    val matchedWanted = user.skillsWanted.firstOrNull { it.lowercase().contains(cleanQuery) }
                    if (matchedWanted != null) {
                        return@mapNotNull UserSearchResult(
                            user = user,
                            matchReason = "Wants to Learn $matchedWanted",
                            matchRank = 2
                        )
                    }

                    // Check Name, Title, Bio, Location
                    val matchesGeneral = user.name.lowercase().contains(cleanQuery) ||
                            user.title.lowercase().contains(cleanQuery) ||
                            user.bio.lowercase().contains(cleanQuery) ||
                            user.location.lowercase().contains(cleanQuery)

                    if (matchesGeneral) {
                        return@mapNotNull UserSearchResult(
                            user = user,
                            matchReason = "Member Match",
                            matchRank = 3
                        )
                    }

                    null
                }.sortedBy { it.matchRank }

                if (results.isEmpty()) {
                    _uiState.value = DiscoverUiState.Empty
                } else {
                    _uiState.value = DiscoverUiState.Success(results)
                }
            }
            .launchIn(this)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onNeighborhoodSelected(neighborhood: String) {
        _selectedNeighborhood.value = neighborhood
    }
}
