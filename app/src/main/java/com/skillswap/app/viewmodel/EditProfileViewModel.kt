package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import com.skillswap.app.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Primary check from active repository
            val localUser = FakeRepository.getUsers().find { it.id == userId } ?: FakeRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(user = localUser, isLoading = false)

            // Sync from Firestore if available
            try {
                val firestoreUser = repository.getUser(userId)
                if (firestoreUser != null) {
                    _uiState.value = _uiState.value.copy(user = firestoreUser)
                    FakeRepository.updateUserProfile(firestoreUser)
                }
            } catch (e: Exception) {
                // Ignore offline sync errors
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(name = name))
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(title = title))
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(email = email))
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(phone = phone))
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(location = location))
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(bio = bio))
    }
    
    fun updateProfileImageUrl(url: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(profileImageUrl = url))
    }

    fun updatePortfolioUrl(url: String) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(portfolioUrl = url))
    }

    fun updateSkillsOffered(skills: List<String>) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(skillsOffered = skills))
    }

    fun updateSkillsWanted(skills: List<String>) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(skillsWanted = skills))
    }

    fun updateCertifications(certs: List<String>) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(certifications = certs))
    }

    fun updateYearsOfExperience(years: Int) {
        _uiState.value = _uiState.value.copy(user = _uiState.value.user?.copy(yearsOfExperience = years))
    }

    fun saveProfile() {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            // Save to active repository & Firestore
            FakeRepository.updateUserProfile(user)
            try {
                repository.saveUser(user)
            } catch (e: Exception) {
                // Ignore offline sync errors
            }

            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }
}
