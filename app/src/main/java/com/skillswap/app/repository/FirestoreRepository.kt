package com.skillswap.app.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.skillswap.app.model.ChatMessage
import com.skillswap.app.model.Swap
import com.skillswap.app.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val swapsCollection = db.collection("swaps")

    suspend fun saveUser(user: User) {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        val docId = when {
            user.id.isNotBlank() -> user.id
            !authUid.isNullOrBlank() -> authUid
            else -> throw IllegalArgumentException("User ID cannot be blank when saving profile.")
        }
        val userToSave = user.copy(id = docId)
        usersCollection.document(docId).set(userToSave).await()
    }

    suspend fun getUser(userId: String): User? {
        if (userId.isBlank()) return null
        return try {
            usersCollection.document(userId).get().await().toObject<User>()
        } catch (e: Exception) {
            null
        }
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

    // --- REAL-TIME CHAT IMPLEMENTATION ---

    fun getConversationId(uid1: String, uid2: String): String {
        if (uid1.isBlank() || uid2.isBlank()) return "general_chat"
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>> {
        if (conversationId.isBlank()) return flowOf(emptyList())
        return db.collection("chats")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatMessage::class.java)
            }
            .catch { emit(emptyList()) }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        text: String
    ) {
        if (text.isBlank() || conversationId.isBlank()) return
        val messageRef = db.collection("chats")
            .document(conversationId)
            .collection("messages")
            .document()

        val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        val chatMessage = ChatMessage(
            id = messageRef.id,
            senderId = senderId,
            receiverId = receiverId,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            formattedTime = formattedTime
        )

        messageRef.set(chatMessage).await()

        val conversationData = mapOf(
            "conversationId" to conversationId,
            "participantIds" to listOf(senderId, receiverId).distinct(),
            "lastMessage" to text.trim(),
            "lastMessageAt" to System.currentTimeMillis()
        )
        db.collection("chats").document(conversationId).set(conversationData, SetOptions.merge()).await()
    }
}
