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

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        val targetId = if (userId.isNotBlank()) userId else FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (targetId.isBlank()) {
            _uiState.value = ProfileUiState(error = "User not logged in.", isLoading = false)
            return
        }

        // Fast memory lookup first
        val cachedUser = FakeRepository.getUsers().find { it.id == targetId }
        if (cachedUser != null) {
            _uiState.value = ProfileUiState(user = cachedUser, isLoading = false)
        } else {
            _uiState.value = ProfileUiState(isLoading = true)
        }

        // Async Firestore sync
        viewModelScope.launch {
            try {
                val firestoreUser = repository.getUser(targetId)
                if (firestoreUser != null) {
                    _uiState.value = ProfileUiState(user = firestoreUser, isLoading = false)
                    FakeRepository.updateUserProfile(firestoreUser)
                } else if (cachedUser == null) {
                    _uiState.value = ProfileUiState(error = "User profile not found.", isLoading = false)
                }
            } catch (e: Exception) {
                if (_uiState.value.user == null) {
                    _uiState.value = ProfileUiState(error = e.message ?: "Failed to load profile.", isLoading = false)
                }
            }
        }
    }
}
