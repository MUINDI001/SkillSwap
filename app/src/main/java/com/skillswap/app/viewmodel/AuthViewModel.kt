package com.skillswap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.skillswap.app.model.User
import com.skillswap.app.repository.FakeRepository
import com.skillswap.app.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository = FirestoreRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val existingUser = repository.getUser(currentUser.uid)
                    if (existingUser != null) {
                        FakeRepository.registerUser(existingUser)
                    } else {
                        val newUser = User(
                            id = currentUser.uid,
                            name = currentUser.displayName?.takeIf { it.isNotBlank() } ?: currentUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Accra Member",
                            email = currentUser.email ?: "",
                            location = "East Legon, Accra",
                            skillsOffered = listOf("Skill Exchange"),
                            skillsWanted = listOf("Mentorship"),
                            rating = 5.0,
                            ratingCount = 1,
                            bio = "Active SkillSwap Accra member."
                        )
                        FakeRepository.registerUser(newUser)
                        try { repository.saveUser(newUser) } catch (e: Exception) {}
                    }
                } catch (e: Exception) {
                    // Fallback to local
                }
            }
            _authState.value = AuthState.Authenticated(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser!!
                        viewModelScope.launch {
                            try {
                                val firestoreUser = repository.getUser(firebaseUser.uid)
                                if (firestoreUser != null) {
                                    FakeRepository.registerUser(firestoreUser)
                                } else {
                                    val newUser = User(
                                        id = firebaseUser.uid,
                                        name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: firebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Accra Member",
                                        email = firebaseUser.email ?: email,
                                        location = "East Legon, Accra",
                                        skillsOffered = listOf("Skill Exchange"),
                                        skillsWanted = listOf("Mentorship"),
                                        rating = 5.0,
                                        ratingCount = 1,
                                        bio = "Active SkillSwap Accra member."
                                    )
                                    FakeRepository.registerUser(newUser)
                                    try { repository.saveUser(newUser) } catch (e: Exception) {}
                                }
                            } catch (e: Exception) {
                                // Ignore offline sync issues
                            }
                            _authState.value = AuthState.Authenticated(firebaseUser)
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                    }
                }
        }
    }

    fun loginWithDemoUser(demoUserId: String) {
        FakeRepository.setCurrentUser(demoUserId)
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            _authState.value = AuthState.Authenticated(firebaseUser)
        } else {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                auth.signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val anonUser = auth.currentUser!!
                            val demoUser = FakeRepository.getCurrentUser()
                            val syncedDemoUser = demoUser.copy(id = anonUser.uid)
                            FakeRepository.registerUser(syncedDemoUser)
                            viewModelScope.launch {
                                try { repository.saveUser(syncedDemoUser) } catch (e: Exception) {}
                            }
                            _authState.value = AuthState.Authenticated(anonUser)
                        } else {
                            val mockUser = FakeRepository.getCurrentUser()
                            _authState.value = AuthState.Authenticated(
                                auth.currentUser ?: return@addOnCompleteListener
                            )
                        }
                    }
            }
        }
    }

    fun signUpWithDetails(
        name: String,
        email: String,
        location: String,
        pass: String,
        confirmPass: String
    ) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Name, email and password are required")
            return
        }

        if (pass != confirmPass) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        if (pass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser!!
                        val newUser = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = email,
                            location = if (location.isNotBlank()) location else "East Legon, Accra",
                            skillsOffered = listOf("Skill Exchange"),
                            skillsWanted = listOf("Mentorship"),
                            rating = 5.0,
                            ratingCount = 1,
                            bio = "New SkillSwap member in Accra."
                        )

                        FakeRepository.registerUser(newUser)
                        viewModelScope.launch {
                            try {
                                repository.saveUser(newUser)
                            } catch (e: Exception) {
                                // Ignore offline error
                            }
                        }

                        _authState.value = AuthState.Authenticated(firebaseUser)
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign up failed")
                    }
                }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
