package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.model.Swap
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class RequestsUiState {
    object Loading : RequestsUiState()
    data class Success(val swaps: List<Swap>) : RequestsUiState()
    object Empty : RequestsUiState()
}

class RequestsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    
    val uiState: StateFlow<RequestsUiState> = combine(
        FakeRepository.swaps,
        _isLoading
    ) { swaps, loading ->
        when {
            loading -> RequestsUiState.Loading
            swaps.isEmpty() -> RequestsUiState.Empty
            else -> RequestsUiState.Success(swaps)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestsUiState.Loading
    )

    init {
        simulateInitialLoading()
    }

    private fun simulateInitialLoading() {
        viewModelScope.launch {
            delay(800) // Small delay for polish
            _isLoading.value = false
        }
    }

    fun sendRequest(from: User, to: User) {
        viewModelScope.launch {
            FakeRepository.sendSwapRequest(from, to)
        }
    }

    fun acceptRequest(swap: Swap) {
        viewModelScope.launch {
            FakeRepository.acceptRequest(swap.id)
        }
    }

    fun declineRequest(swap: Swap) {
        viewModelScope.launch {
            FakeRepository.declineRequest(swap.id)
        }
    }
}
