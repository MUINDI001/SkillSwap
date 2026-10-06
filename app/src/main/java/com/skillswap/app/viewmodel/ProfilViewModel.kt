package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            delay(1000) // Simulate network delay
            val user = FakeRepository.getUsers().find { it.id == userId }
            if (user != null) {
                _uiState.value = ProfileUiState(user = user, isLoading = false)
            } else {
                _uiState.value = ProfileUiState(error = "User not found", isLoading = false)
            }
        }
    }
}
