package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class DiscoverUiState {
    object Idle : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Success(val users: List<User>) : DiscoverUiState()
    object Empty : DiscoverUiState()
}

class DiscoverViewModel : ViewModel() {

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
            combine(_searchQuery.debounce(200), _selectedNeighborhood) { query, neighborhood ->
                Pair(query, neighborhood)
            }
            .onEach { (query, neighborhood) ->
                _uiState.value = DiscoverUiState.Loading
                delay(300)
                
                val allUsers = FakeRepository.getUsers()
                val filtered = allUsers.filter { user ->
                    val matchesQuery = query.isEmpty() ||
                            user.name.contains(query, ignoreCase = true) ||
                            user.location.contains(query, ignoreCase = true) ||
                            user.skillsOffered.any { skill -> skill.contains(query, ignoreCase = true) } ||
                            user.skillsWanted.any { skill -> skill.contains(query, ignoreCase = true) }

                    val matchesNeighborhood = neighborhood == "All" ||
                            user.location.contains(neighborhood, ignoreCase = true)

                    matchesQuery && matchesNeighborhood
                }

                if (filtered.isEmpty()) {
                    _uiState.value = DiscoverUiState.Empty
                } else {
                    _uiState.value = DiscoverUiState.Success(filtered)
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
