package com.skillswap.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.skillswap.app.model.Swap
import com.skillswap.app.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val swapsCollection = db.collection("swaps")

    suspend fun saveUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersCollection.document(userId).get().await().toObject<User>()
    }
    
    fun getUserFlow(userId: String): Flow<User?> {
        return usersCollection.document(userId).snapshots()
            .map { it.toObject<User>() }
            .catch { emit(null) }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            usersCollection.get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getAllUsersFlow(): Flow<List<User>> {
        return usersCollection.snapshots()
            .map { it.toObjects(User::class.java) }
            .catch { emit(emptyList()) }
    }

    suspend fun seedDatabase(users: List<User>) {
        users.forEach { user ->
            try {
                usersCollection.document(user.id).set(user).await()
            } catch (e: Exception) {
                // Ignore seeding errors
            }
        }
    }

    suspend fun sendSwapRequest(swap: Swap) {
        swapsCollection.document(swap.id).set(swap).await()
    }

    fun getSwapsForUser(userId: String): Flow<List<Swap>> {
        return swapsCollection
            .whereArrayContains("participantIds", userId)
            .snapshots()
            .map { it.toObjects(Swap::class.java) }
            .catch { emit(emptyList()) }
    }

    suspend fun updateSwapStatus(swapId: String, status: String) {
        swapsCollection.document(swapId).update("status", status).await()
    }

    suspend fun rateUser(userId: String, newRating: Double) {
        val userRef = usersCollection.document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject<User>() ?: return@runTransaction
            
            val totalRatingValue = user.rating * user.ratingCount
            val newCount = user.ratingCount + 1
            val updatedRating = (totalRatingValue + newRating) / newCount
            
            transaction.update(userRef, "rating", updatedRating)
            transaction.update(userRef, "ratingCount", newCount)
        }.await()
    }
}
