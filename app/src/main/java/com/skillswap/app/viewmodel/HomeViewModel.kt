package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import com.skillswap.app.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val users: List<User>) : HomeUiState()
    object Empty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshUsers()
    }

    fun refreshUsers() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: FakeRepository.getCurrentUser().id
            
            var allUsers = try {
                repository.getAllUsers()
            } catch (e: Exception) {
                emptyList()
            }

            if (allUsers.isEmpty()) {
                allUsers = FakeRepository.getUsers()
            } else {
                // Combine with local demo users ensuring no duplicates
                val demoUsers = FakeRepository.getUsers()
                val merged = (allUsers + demoUsers).distinctBy { it.id }
                allUsers = merged
            }

            val filtered = allUsers.filter { it.id != currentUid && it.id.isNotBlank() }

            if (filtered.isEmpty()) {
                _uiState.value = HomeUiState.Empty
            } else {
                _uiState.value = HomeUiState.Success(filtered)
            }
        }
    }
}
