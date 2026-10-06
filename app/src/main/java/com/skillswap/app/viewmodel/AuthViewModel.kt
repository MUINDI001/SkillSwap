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
                        _authState.value = AuthState.Authenticated(firebaseUser)
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
            // Log in anonymously to get real Firebase auth session
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                auth.signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Authenticated(auth.currentUser!!)
                        } else {
                            // Fallback if offline
                            val mockUser = FakeRepository.getCurrentUser()
                            _authState.value = AuthState.Authenticated(
                                auth.currentUser ?: auth.currentUser ?: return@addOnCompleteListener
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

                        // Register in repositories
                        FakeRepository.registerUser(newUser)
                        viewModelScope.launch {
                            try {
                                repository.saveUser(newUser)
                            } catch (e: Exception) {
                                // Ignore firestore offline error
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
